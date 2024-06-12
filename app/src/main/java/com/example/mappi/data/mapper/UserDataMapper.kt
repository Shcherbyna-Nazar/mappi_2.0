package com.example.mappi.data.mapper

import com.example.mappi.data.datasource.remote.dto.UserDto
import com.example.mappi.domain.model.UserData

object UserDataMapper {
    fun mapToDomain(userDto: UserDto): UserData {
        return UserData(
            userId = userDto.userId,
            userName = userDto.userName,
            email = userDto.email,
            profilePictureUrl = userDto.profilePictureUrl,
            requestStatus = userDto.requestStatus
        )
    }
}