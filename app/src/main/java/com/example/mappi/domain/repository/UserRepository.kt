package com.example.mappi.domain.repository

import com.example.mappi.domain.model.FriendRequest
import com.example.mappi.domain.model.UserData

interface UserRepository {
    suspend fun getFriends(): List<UserData>
    suspend fun searchFriends(query: String): List<UserData>
    suspend fun sendRequest(friendId: String)
    suspend fun acceptRequest(friendId: String)
    suspend fun rejectRequest(friendId: String)
    suspend fun getFriendRequests(): List<FriendRequest>
    suspend fun getUserById(userId: String): UserData
    suspend fun deleteFriend(friendId: String)
}