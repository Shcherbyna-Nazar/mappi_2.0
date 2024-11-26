package com.example.mappi.domain.use_case.decisions

import com.example.mappi.domain.model.Restaurant
import com.example.mappi.domain.model.UserDecision
import com.example.mappi.util.thompson.ThompsonUtils
import javax.inject.Inject

class GetRestaurantRecommendationUseCase @Inject constructor(
    private val getDecisionsUseCase: GetDecisionsUseCase
) {
    suspend operator fun invoke(restaurants: List<Restaurant>): Restaurant? {
        println("restaurants: $restaurants")
        val stats = getDecisionsUseCase(restaurants.map { it.id })
        val result = restaurants.maxByOrNull { restaurant ->
            val stat = stats[restaurant.id] ?: UserDecision(restaurant.id)
            ThompsonUtils.thompsonSampleBeta(stat.successCount, stat.failureCount)
        }
        println("result: $result")
        return result
    }
}
