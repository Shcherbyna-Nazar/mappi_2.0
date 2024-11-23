package com.example.mappi.domain.use_case.decisions

import android.location.Location
import com.example.mappi.BuildConfig
import com.example.mappi.domain.model.Restaurant
import com.example.mappi.util.NearbyUtils
import javax.inject.Inject

class GetNearbyRestaurantsUseCase @Inject constructor() {
    suspend operator fun invoke(userLocation: Location): List<Restaurant> {
        val nearbyUtils = NearbyUtils(BuildConfig.API_KEY)
        return nearbyUtils.fetchNearbyRestaurants(userLocation)
    }
}