package com.example.mappi.domain.model

data class UserDecision(
    val placeId: String,
    val successCount: Int = 0,
    val failureCount: Int = 0
)

