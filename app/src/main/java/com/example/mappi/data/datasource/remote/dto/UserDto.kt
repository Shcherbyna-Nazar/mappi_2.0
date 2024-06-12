package com.example.mappi.data.datasource.remote.dto

import com.example.mappi.domain.model.RequestStatus

data class UserDto(
    val userId: String,
    val userName: String?,
    val email: String?,
    val profilePictureUrl: String?,
    val requestStatus: RequestStatus = RequestStatus.NONE
){
    constructor(): this("", "", "", "", RequestStatus.NONE)
}