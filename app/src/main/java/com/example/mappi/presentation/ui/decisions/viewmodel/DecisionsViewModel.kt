package com.example.mappi.presentation.ui.decisions.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Restaurant
import com.example.mappi.domain.use_case.decisions.GetNearbyRestaurantsUseCase
import com.example.mappi.domain.use_case.decisions.GetRestaurantRecommendationUseCase
import com.example.mappi.domain.use_case.decisions.MakeDecisionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecisionsViewModel @Inject constructor(
    private val makeDecisionUseCase: MakeDecisionUseCase,
    private val getRecommendationUseCase: GetRestaurantRecommendationUseCase,
    private val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase
) : ViewModel() {

    private val _restaurantRecommendation = MutableStateFlow<Restaurant?>(null)
    val restaurantRecommendation: StateFlow<Restaurant?> = _restaurantRecommendation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nearbyRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val nearbyRestaurants: StateFlow<List<Restaurant>> = _nearbyRestaurants.asStateFlow()


    fun prefetchNearbyRestaurants(userLocation: Location) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.e("DecisionsViewModel", "prefetchNearbyRestaurants: start")
                val nearbyRestaurants = getNearbyRestaurantsUseCase(userLocation)
                _nearbyRestaurants.value = nearbyRestaurants
                Log.e("DecisionsViewModel", "prefetchNearbyRestaurants: ${nearbyRestaurants.size}")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetches a restaurant recommendation based on the user's current location.
     * Updates [_restaurantRecommendation] with the recommended restaurant.
     */
    fun fetchRecommendation() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val recommendedRestaurant = getRecommendationUseCase(nearbyRestaurants.value)
                _restaurantRecommendation.value = recommendedRestaurant
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Records the user's decision (like or dislike) on a restaurant and refreshes the recommendation.
     * If the decision is successful, fetches a new recommendation.
     */
    fun makeDecision(placeId: String, decision: Boolean) {
        viewModelScope.launch {
            try {
                makeDecisionUseCase(placeId, decision)
                fetchRecommendation() // Refresh recommendation after decision
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error processing decision"
            }
        }
    }
}
