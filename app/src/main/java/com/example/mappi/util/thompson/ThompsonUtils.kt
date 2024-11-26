package com.example.mappi.util.thompson

import org.apache.commons.math3.distribution.GammaDistribution

object ThompsonUtils {
    fun thompsonSampleBeta(successes: Int, failures: Int): Double {
        val alpha = 1.0 + successes
        val beta = 1.0 + failures

        val gamma1 = GammaDistribution(alpha, 1.0)
        val gamma2 = GammaDistribution(beta, 1.0)

        val sample1 = gamma1.sample()
        val sample2 = gamma2.sample()

        return sample1 / (sample1 + sample2)
    }
}
