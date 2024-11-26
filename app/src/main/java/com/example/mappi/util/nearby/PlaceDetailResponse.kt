package com.example.mappi.util.nearby

import com.google.gson.annotations.SerializedName

data class PlaceDetailsResponse(
    @SerializedName("result") val result: Result?
) {
    data class Result(
        @SerializedName("place_id") val place_id: String,
        @SerializedName("formatted_address") val formatted_address: String?,
        @SerializedName("price_level") val price_level: Int?,
        @SerializedName("types") val types: List<String>?,
        @SerializedName("photos") val photos: List<Photo>?
    )

    data class Photo(
        @SerializedName("photo_reference") val photo_reference: String
    )
}
