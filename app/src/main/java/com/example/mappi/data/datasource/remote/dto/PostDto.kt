package com.example.mappi.data.datasource.remote.dto

data class PostDto(
    val id: String,
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val userName: String,
    val rating: Int,
    val comments: List<CommentDto> = emptyList()
)
