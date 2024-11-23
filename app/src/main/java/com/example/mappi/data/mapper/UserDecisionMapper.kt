package com.example.mappi.data.mapper

import com.example.mappi.domain.model.UserDecision

object UserDecisionMapper {
    fun mapToDomain(
        placeId: String,
        successCount: Int,
        failureCount: Int
    ): UserDecision {
        return UserDecision(
            placeId = placeId,
            successCount = successCount,
            failureCount = failureCount
        )
    }
}