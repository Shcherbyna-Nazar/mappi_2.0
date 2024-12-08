package com.example.mappi.domain.model

data class Post(
    val id: String,
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val userName: String,
    val rating: Int,
    var comments: List<Comment> = emptyList()
)
