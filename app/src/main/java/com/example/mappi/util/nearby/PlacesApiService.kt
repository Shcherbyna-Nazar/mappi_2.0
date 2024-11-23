package com.example.mappi.util.nearby

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    @GET("nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("pagetoken") pageToken: String? = null
    ): Call<NearbySearchResponse>

    @GET("details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<PlaceDetailsResponse>
}
