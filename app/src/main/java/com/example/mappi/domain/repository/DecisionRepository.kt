package com.example.mappi.domain.repository

import com.example.mappi.domain.model.UserDecision

interface DecisionRepository {
    suspend fun makeDecision(placeId: String, decision: Boolean)
    suspend fun getDecisions(placeIds: List<String>): Map<String, UserDecision>
}