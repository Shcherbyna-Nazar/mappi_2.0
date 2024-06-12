package com.example.mappi.data.datasource.remote.dto

import com.example.mappi.domain.model.RequestStatus

data class FriendRequestDto(
    val requestId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: RequestStatus = RequestStatus.NONE
) {
    constructor() : this("", "", "", RequestStatus.NONE)
}
