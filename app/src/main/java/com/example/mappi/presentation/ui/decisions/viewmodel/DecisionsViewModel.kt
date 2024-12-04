package com.example.mappi.presentation.ui.decisions.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Place
import com.example.mappi.domain.use_case.decisions.GetNearbyRestaurantsUseCase
import com.example.mappi.domain.use_case.decisions.GetRestaurantRecommendationUseCase
import com.example.mappi.domain.use_case.decisions.MakeDecisionUseCase
import com.example.mappi.util.nearby.PlaceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecisionsViewModel @Inject constructor(
    private val makeDecisionUseCase: MakeDecisionUseCase,
    private val getRecommendationUseCase: GetRestaurantRecommendationUseCase,
    private val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase
) : ViewModel() {

    private val _placeRecommendation = MutableStateFlow<Place?>(null)
    val placeRecommendation: StateFlow<Place?> = _placeRecommendation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nearbyRestaurants = MutableStateFlow<List<Place>>(emptyList())

    private val _lastLocation = MutableStateFlow<Location?>(null)

    @Volatile
    private var requestNumber = 0

    private suspend fun prefetchNearbyRestaurants(
        userLocation: Location,
        placeTypes: List<PlaceType> = listOf(PlaceType.RESTAURANT)
    ) {
        _lastLocation.value = userLocation
        try {
            Log.d("DecisionsViewModel", "Prefetching nearby restaurants... $placeTypes")
            val nearbyRestaurants = getNearbyRestaurantsUseCase(userLocation, placeTypes)
            _nearbyRestaurants.value = nearbyRestaurants
            Log.d("DecisionsViewModel", "Found ${nearbyRestaurants.size} restaurants")
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "An unexpected error occurred"
        }
    }

    /**
     * Fetches a restaurant recommendation based on the user's current location.
     * Waits for any necessary prefetching of nearby restaurants.
     */
    @Synchronized
    fun fetchRecommendation(
        userLocation: Location,
        checkDistance: Boolean = true,
        placeTypes: List<PlaceType> = listOf(PlaceType.RESTAURANT),
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            requestNumber++
            _isLoading.value = true
            _error.value = null
            try {
                if (forceRefresh) {
                    prefetchNearbyRestaurants(userLocation, placeTypes)
                } else {
                    val lastLocation = _lastLocation.value
                    if (lastLocation == null ||
                        (checkDistance && lastLocation.distanceTo(userLocation) > 1000)
                    ) {
                        Log.d("DecisionsViewModel", "Location change detected, prefetching...")
                        prefetchNearbyRestaurants(userLocation, placeTypes)
                    }
                }

                val recommendedRestaurant = getRecommendationUseCase(_nearbyRestaurants.value)
                _placeRecommendation.value = recommendedRestaurant
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                requestNumber--
                if(requestNumber == 0) {
                    _isLoading.value = false
                }
                Log.d(
                    "DecisionsViewModel",
                    "Fetched recommendation: ${_placeRecommendation.value?.name}"
                )
            }
        }
    }

    /**
     * Records the user's decision (like or dislike) on a restaurant and refreshes the recommendation.
     * If the decision is successful, fetches a new recommendation.
     */
    fun makeDecision(
        userLocation: Location,
        placeId: String,
        decision: Boolean
    ) {
        viewModelScope.launch {
            try {
                makeDecisionUseCase(placeId, decision)
                if (!decision) {
                    fetchRecommendation(userLocation)
                } // Refresh recommendation after decision
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error processing decision"
            }
        }
    }
}

