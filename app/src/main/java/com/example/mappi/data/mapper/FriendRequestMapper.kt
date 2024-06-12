package com.example.mappi.data.mapper

import com.example.mappi.domain.model.FriendRequest
import com.example.mappi.domain.model.RequestStatus
import com.example.mappi.domain.model.UserData

object FriendRequestMapper {
    fun mapToDomain(
        fromUser: UserData,
        toUserData: UserData,
        requestId: String,
        status: RequestStatus
    ): FriendRequest {
        return FriendRequest(
            fromUser = fromUser,
            toUser = toUserData,
            requestId = requestId,
            status = status
        )
    }
}