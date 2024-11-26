package com.example.mappi.data.datasource.remote.dto

data class UserDecisionDto(
    val placeId: String,
    var successCount: Int = 0,
    var failureCount: Int = 0,
){
    constructor(): this("", 0, 0)
}