package com.example.mappi.domain.model

import kotlin.random.Random

data class Post(
    val url: String,
    val latitude: Double,
    val longitude: Double,
) {
    val id = generateId()

    private fun generateId(): Int {
        return url.hashCode() + latitude.hashCode() + longitude.hashCode() + Random.nextInt()
    }
}
