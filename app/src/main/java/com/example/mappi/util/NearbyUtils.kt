package com.example.mappi.util

import android.location.Location
import com.example.mappi.domain.model.Restaurant
import com.example.mappi.util.nearby.NearbySearchResponse
import com.example.mappi.util.nearby.PlacesApiService
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NearbyUtils(private val apiKey: String) {

    private val placesApiService: PlacesApiService =
        Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(PlacesApiService::class.java)

    suspend fun fetchNearbyRestaurants(location: Location, radius: Int = 3000): List<Restaurant> {
        val locationStr = "${location.latitude},${location.longitude}"
        return fetchAllNearbyPlaces(locationStr, location, radius)
    }

    private suspend fun fetchAllNearbyPlaces(
        locationStr: String, originLocation: Location, radius: Int
    ): List<Restaurant> {
        val results = mutableListOf<Restaurant>()
        var pageToken: String? = null

        do {
            val page = fetchPage(locationStr, originLocation, radius, pageToken)
            results += page.restaurants
            pageToken = page.nextPageToken
            pageToken?.let { delay(2000) } // Delay for next page token activation
        } while (pageToken != null && results.size < 31)

        return results
    }

    private suspend fun fetchPage(
        locationStr: String, originLocation: Location, radius: Int, pageToken: String?
    ): NearbySearchResponseData = suspendCoroutine { continuation ->
        val types =
            "restaurant|cafe|meal_takeaway|meal_delivery|bakery|food|grocery_or_supermarket|supermarket"

        placesApiService.getNearbyPlaces(locationStr, radius, types, apiKey, pageToken)
            .enqueue(object : Callback<NearbySearchResponse> {
                override fun onResponse(
                    call: Call<NearbySearchResponse>, response: Response<NearbySearchResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { nearbyResponse ->
                            val filteredRestaurants = nearbyResponse.results.mapNotNull { result ->
                                val distance = calculateDistanceFromLatLng(
                                    originLocation, result.geometry.location
                                )
                                if (distance <= radius) {
                                    val decimalFormat = String.format(Locale.US, "%.2f", distance / 1000)
                                    val location = Location("").apply {
                                        latitude = result.geometry.location.lat
                                        longitude = result.geometry.location.lng
                                    }
                                    Restaurant(id = result.place_id,
                                        name = result.name,
                                        rating = result.rating ?: 0.0,
                                        distance = decimalFormat.toDouble(),
                                        address = result.vicinity,
                                        cuisineType = inferCuisineType(result.name),
                                        priceRange = mapPriceLevelToRange(result.price_level),
                                        photoUrl = result.photos?.firstOrNull()
                                            ?.let { getPhotoUrl(it.photo_reference) } ?: "",
                                        location = location)
                                } else null
                            }
                            continuation.resume(
                                NearbySearchResponseData(
                                    filteredRestaurants, nearbyResponse.nextPageToken
                                )
                            )
                        } ?: continuation.resumeWithException(Exception("Response body is null"))
                    } else {
                        continuation.resumeWithException(Exception("Failed to fetch nearby places: ${response.errorBody()}"))
                    }
                }

                override fun onFailure(call: Call<NearbySearchResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
    }

    data class NearbySearchResponseData(
        val restaurants: List<Restaurant>, val nextPageToken: String?
    )

    private fun calculateDistanceFromLatLng(
        originLocation: Location, placeLocation: NearbySearchResponse.LatLngLiteral
    ): Double {
        val restaurantLocation = Location("").apply {
            latitude = placeLocation.lat
            longitude = placeLocation.lng
        }
        return originLocation.distanceTo(restaurantLocation).toDouble()
    }

    private fun mapPriceLevelToRange(priceLevel: Int?): String = when (priceLevel) {
        0 -> "$"
        1 -> "$$"
        2 -> "$$$"
        3 -> "$$$$"
        else -> "Unknown"
    }

    private fun inferCuisineType(name: String?): String {
        val cuisineKeywords = mapOf(
            "Pizza" to "Italian",
            "Sushi" to "Japanese",
            "Burger" to "American",
            "Grill" to "Barbecue",
            "Taco" to "Mexican",
            "Indian" to "Indian",
            "BBQ" to "Barbecue",
            "Pasta" to "Italian",
            "Seafood" to "Seafood",
            "Steak" to "Steakhouse",
            "Noodles" to "Asian",
            "Ramen" to "Japanese",
            "Kebab" to "Middle Eastern",
            "Falafel" to "Middle Eastern",
            "Deli" to "Delicatessen",
            "Brasserie" to "French",
            "Bistro" to "French",
            "Burrito" to "Mexican",
            "Bagel" to "Bakery",
            "Smoothie" to "Healthy",
            "Sandwich" to "American",
            "Tavern" to "Bar"
        )

        return cuisineKeywords.entries.firstOrNull {
            name?.contains(
                it.key, ignoreCase = true
            ) == true
        }?.value ?: "General"
    }

    private fun getPhotoUrl(photoReference: String): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photoReference&key=$apiKey"
    }
}
