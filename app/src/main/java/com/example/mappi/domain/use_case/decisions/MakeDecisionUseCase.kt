package com.example.mappi.domain.use_case.decisions

import com.example.mappi.domain.repository.DecisionRepository
import javax.inject.Inject

class MakeDecisionUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository
) {
    suspend operator fun invoke(placeId: String, decision: Boolean) {
        decisionRepository.makeDecision(placeId, decision)
    }
}