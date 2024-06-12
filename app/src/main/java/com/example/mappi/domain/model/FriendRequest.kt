package com.example.mappi.domain.model

data class FriendRequest(
    val requestId: String,
    val fromUser: UserData,
    val toUser: UserData,
    val status: RequestStatus
)
