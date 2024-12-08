package com.example.mappi.domain.model

data class Comment(
    val text: String,
    val userName: String,
    val ownerId: String,
    val timeStamp: Long,
    val profilePictureUrl: String
)
