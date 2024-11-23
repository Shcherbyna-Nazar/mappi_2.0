package com.example.mappi.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: Double,// in km
    var address: String = "",
    var priceRange: String = "",//$$$
    var types: String = "",
    var photoUrl: String = "",
    var cuisineType: String = ""
)
