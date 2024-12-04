package com.example.mappi.domain.use_case.decisions

import com.example.mappi.domain.model.Place
import com.example.mappi.domain.model.UserDecision
import com.example.mappi.util.thompson.ThompsonUtils
import javax.inject.Inject

class GetRestaurantRecommendationUseCase @Inject constructor(
    private val getDecisionsUseCase: GetDecisionsUseCase
) {
    suspend operator fun invoke(places: List<Place>): Place? {
        println("restaurants: $places")
        val stats = getDecisionsUseCase(places.map { it.id })
        val result = places.maxByOrNull { restaurant ->
            val stat = stats[restaurant.id] ?: UserDecision(restaurant.id)
            ThompsonUtils.thompsonSampleBeta(stat.successCount, stat.failureCount)
        }
        println("result: $result")
        return result
    }
}
