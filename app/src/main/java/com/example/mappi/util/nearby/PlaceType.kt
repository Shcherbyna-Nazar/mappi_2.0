package com.example.mappi.util.nearby

import com.example.mappi.R

enum class PlaceType(
    val apiType: String,
    val displayName: String,
    val iconResId: Int
) {
    RESTAURANT(
        "restaurant",
        "Restaurant",
        R.drawable.ic_restaurant
    ),
    MUSEUM(
        "museum",
        "Museum",
        R.drawable.ic_museum,
    ),
    GROCERY_OR_SUPERMARKET(
        "grocery_or_supermarket",
        "Grocery or Supermarket",
        R.drawable.ic_grocery,
    ),
    HOTEL(
        "lodging",
        "Hotel",
        R.drawable.ic_hotel,
    );

    companion object {
        fun fromApiType(apiType: String): PlaceType? {
            return values().firstOrNull { it.apiType.equals(apiType, ignoreCase = true) }
        }

    }
}