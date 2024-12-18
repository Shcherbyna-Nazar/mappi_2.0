package com.example.mappi.data.repository

import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.datasource.remote.dto.UserDto
import com.example.mappi.data.mapper.FriendRequestMapper
import com.example.mappi.data.mapper.UserDataMapper
import com.example.mappi.domain.model.FriendRequest
import com.example.mappi.domain.model.RequestStatus
import com.example.mappi.domain.model.UserData
import com.example.mappi.domain.repository.UserRepository

class FirebaseUserRepository(
    private val dataSource: FirebaseDataSource,
) : UserRepository {

    override suspend fun getFriends(): List<UserData> {
        val userDtos = dataSource.getFriends()
        return userDtos.map { UserDataMapper.mapToDomain(it.copy(requestStatus = RequestStatus.ACCEPTED)) }
    }

    override suspend fun searchFriends(query: String): List<UserData> {
        val userDtos = dataSource.searchUsers(query)
        return userDtos.map { UserDataMapper.mapToDomain(it) }
    }

    override suspend fun sendRequest(friendId: String) {
        dataSource.sendFriendRequest(friendId)
    }

    override suspend fun acceptRequest(friendId: String) {
        dataSource.acceptFriendRequest(friendId)
    }

    override suspend fun rejectRequest(friendId: String) {
        dataSource.rejectFriendRequest(friendId)
    }

    override suspend fun getFriendRequests(): List<FriendRequest> {
        val friendRequests = dataSource.getFriendRequests()
        return friendRequests.map { requestDto ->
            val fromUser = dataSource.getUserById(requestDto.fromUserId) ?: UserDto()
            val toUser = dataSource.getUserById(requestDto.toUserId) ?: UserDto()
            val status = requestDto.status
            FriendRequestMapper.mapToDomain(
                UserDataMapper.mapToDomain(fromUser),
                UserDataMapper.mapToDomain(toUser),
                requestDto.requestId,
                status
            )
        }
    }

    override suspend fun getUserById(userId: String): UserData {
        val userDto = dataSource.getUserById(userId) ?: UserDto()
        return UserDataMapper.mapToDomain(userDto)
    }

    override suspend fun deleteFriend(friendId: String) {
        dataSource.deleteFriend(friendId)
    }

    override suspend fun updateUserProfile(userName: String, email: String) {
        dataSource.updateUserProfile(userName, email)
    }
}

