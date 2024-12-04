package com.example.mappi.util.nearby

import android.location.Location
import android.util.Log
import com.example.mappi.domain.model.Place
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)

    suspend fun fetchNearbyPlaces(
        location: Location,
        radius: Int = 3000,
        placeTypes: List<PlaceType> = listOf(PlaceType.RESTAURANT),
        delayBetweenRequests: Long = 2000L,
        maxResults: Int = 40
    ): List<Place> = coroutineScope {
        val locationStr = "${location.latitude},${location.longitude}"
        val mutex = Mutex() // Ensures thread-safe additions to results
        val results = mutableListOf<Place>()

        // Fetch places for each type in parallel
        val fetchJobs = placeTypes.map { placeType ->
            async {
                try {
                    val places = fetchPlacesByType(locationStr, location, radius, placeType, maxResults / placeTypes.size, delayBetweenRequests)
                    mutex.withLock { results.addAll(places) }
                    Log.d("NearbyUtils", "Fetched ${places.size} places of type $placeType")
                } catch (e: Exception) {
                    Log.e("NearbyUtils", "Error fetching places for type $placeType: ${e.message}", e)
                }
            }
        }

        fetchJobs.forEach { it.await() }
        results
    }

    private suspend fun fetchPlacesByType(
        locationStr: String,
        originLocation: Location,
        radius: Int,
        placeType: PlaceType,
        limit: Int,
        delayBetweenRequests: Long
    ): List<Place> {
        val results = mutableListOf<Place>()
        var pageToken: String? = null

        do {
            val page = fetchPage(locationStr, originLocation, radius, placeType, pageToken)
            results += page.places
            pageToken = page.nextPageToken
            pageToken?.let { delay(delayBetweenRequests) }
        } while (pageToken != null && results.size < limit)

        return results.take(limit)
    }

    private suspend fun fetchPage(
        locationStr: String,
        originLocation: Location,
        radius: Int,
        placeType: PlaceType,
        pageToken: String?
    ): NearbySearchResponseData = suspendCoroutine { continuation ->
        placesApiService.getNearbyPlaces(locationStr, radius, placeType.apiType, apiKey, pageToken)
            .enqueue(object : Callback<NearbySearchResponse> {
                override fun onResponse(
                    call: Call<NearbySearchResponse>,
                    response: Response<NearbySearchResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { nearbyResponse ->
                            val filteredPlaces = nearbyResponse.results.mapNotNull { result ->
                                val distance = calculateDistanceFromLatLng(originLocation, result.geometry.location)
                                if (distance <= radius) {
                                    val formattedDistance = String.format(Locale.US, "%.2f", distance / 1000)
                                    val location = Location("").apply {
                                        latitude = result.geometry.location.lat
                                        longitude = result.geometry.location.lng
                                    }
                                    Place(
                                        id = result.place_id,
                                        name = result.name,
                                        rating = result.rating ?: 0.0,
                                        distance = formattedDistance.toDouble(),
                                        address = result.vicinity,
                                        placeType = placeType,
                                        priceRange = mapPriceLevelToRange(result.price_level),
                                        photoUrl = result.photos?.firstOrNull()?.let { getPhotoUrl(it.photo_reference) } ?: "",
                                        location = location
                                    )
                                } else null
                            }
                            continuation.resume(NearbySearchResponseData(filteredPlaces, nearbyResponse.nextPageToken))
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

    private fun calculateDistanceFromLatLng(
        originLocation: Location,
        placeLocation: NearbySearchResponse.LatLngLiteral
    ): Double {
        val targetLocation = Location("").apply {
            latitude = placeLocation.lat
            longitude = placeLocation.lng
        }
        return originLocation.distanceTo(targetLocation).toDouble()
    }

    private fun mapPriceLevelToRange(priceLevel: Int?): String = when (priceLevel) {
        0 -> "$"
        1 -> "$$"
        2 -> "$$$"
        3 -> "$$$$"
        else -> "Unknown"
    }

    private fun getPhotoUrl(photoReference: String): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$photoReference&key=$apiKey"
    }
}
