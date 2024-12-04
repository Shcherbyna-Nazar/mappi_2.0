package com.example.mappi.domain.use_case.decisions

import android.location.Location
import com.example.mappi.BuildConfig
import com.example.mappi.domain.model.Place
import com.example.mappi.util.nearby.NearbyUtils
import com.example.mappi.util.nearby.PlaceType
import javax.inject.Inject

class GetNearbyRestaurantsUseCase @Inject constructor() {
    suspend operator fun invoke(
        userLocation: Location,
        placeTypes: List<PlaceType> = listOf(PlaceType.RESTAURANT)
    ): List<Place> {
        val nearbyUtils = NearbyUtils(BuildConfig.API_KEY)
        return nearbyUtils.fetchNearbyPlaces(userLocation, placeTypes = placeTypes)
    }
}