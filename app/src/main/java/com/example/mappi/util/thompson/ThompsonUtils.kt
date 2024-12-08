package com.example.mappi.util.thompson

import org.apache.commons.math3.distribution.BetaDistribution

object ThompsonUtils {
    fun thompsonSampleBeta(successes: Int, failures: Int): Double {
        val alpha = 1.0 + successes
        val beta = 1.0 + failures

        val betaDistribution = BetaDistribution(alpha, beta)
        return betaDistribution.sample()
    }
}
