package com.example.mappi.util.nearby

import com.google.gson.annotations.SerializedName

data class NearbySearchResponse(
    @SerializedName("results") val results: List<Result>,
    @SerializedName("next_page_token") val nextPageToken: String?
) {
    data class Result(
        @SerializedName("place_id") val place_id: String,
        @SerializedName("name") val name: String,
        @SerializedName("rating") val rating: Double?,
        @SerializedName("price_level") val price_level: Int?,
        @SerializedName("types") val types: List<String>?,
        @SerializedName("photos") val photos: List<Photo>?,
        @SerializedName("geometry") val geometry: Geometry,
        @SerializedName("vicinity") val vicinity: String,
    )

    data class Geometry(
        @SerializedName("location") val location: LatLngLiteral
    )

    data class LatLngLiteral(
        @SerializedName("lat") val lat: Double,
        @SerializedName("lng") val lng: Double
    )

    data class Photo(
        @SerializedName("photo_reference") val photo_reference: String
    )
}
