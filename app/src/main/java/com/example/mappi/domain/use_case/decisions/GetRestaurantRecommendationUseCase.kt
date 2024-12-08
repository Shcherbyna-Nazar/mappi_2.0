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
            calculateScore(restaurant, stat)
        }
        println("result: $result")
        return result
    }

    private fun calculateScore(place: Place, userDecision: UserDecision): Double {
        val normalizedRating = place.rating / 5.0
        val normalizedDistance = if (place.distance > 0) 1.0 / (1.0 + place.distance) else 1.0
        val normalizedPriceRange = normalizePriceRange(place.priceRange)

        val userDecisionImpact =
            ThompsonUtils.thompsonSampleBeta(userDecision.successCount, userDecision.failureCount)

        val weightedRating = 0.2
        val weightedDistance = 0.4
        val weightedUserDecision = 0.3
        val weightedPriceRange = 0.1

        return weightedRating * normalizedRating +
                weightedDistance * normalizedDistance +
                weightedPriceRange * normalizedPriceRange +
                weightedUserDecision * userDecisionImpact
    }

    private fun normalizePriceRange(priceRange: String): Double {
        return when (priceRange) {
            "$" -> 1.0
            "$$" -> 0.5
            "$$$" -> 0.25
            "$$$$" -> 0.1
            else -> 0.0
        }
    }
}
