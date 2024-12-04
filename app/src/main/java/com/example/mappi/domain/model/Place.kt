package com.example.mappi.domain.model

import android.location.Location
import com.example.mappi.util.nearby.PlaceType

data class Place(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: Double,// in km
    var address: String = "",
    val placeType: PlaceType,
    var priceRange: String = "",//$$$
    var types: String = "",
    var photoUrl: String = "",
    val location: Location,
)
