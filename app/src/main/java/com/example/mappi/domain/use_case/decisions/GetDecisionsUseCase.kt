package com.example.mappi.domain.use_case.decisions

import com.example.mappi.domain.repository.DecisionRepository
import javax.inject.Inject

class GetDecisionsUseCase @Inject constructor(
    private val decisionRepository: DecisionRepository
) {
    suspend operator fun invoke(placeIds: List<String>) = decisionRepository.getDecisions(placeIds)
}