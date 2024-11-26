package com.example.mappi.data.repository

import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.mapper.UserDecisionMapper
import com.example.mappi.domain.model.UserDecision
import com.example.mappi.domain.repository.DecisionRepository

class FirebaseDecisionRepository(
    private val dataSource: FirebaseDataSource
) : DecisionRepository {
    override suspend fun getDecisions(placeIds: List<String>): Map<String, UserDecision> {
        return dataSource.getUserDecisions(placeIds)
            .mapValues {
                UserDecisionMapper.mapToDomain(
                    it.value.placeId,
                    it.value.successCount,
                    it.value.failureCount
                )
            }
    }

    override suspend fun makeDecision(placeId: String, decision: Boolean) {
        dataSource.makeDecision(placeId, decision)
    }
}