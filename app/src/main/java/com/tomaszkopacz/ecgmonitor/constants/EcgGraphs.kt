package com.tomaszkopacz.ecgmonitor.constants

object EcgGraphs {

    object RealEcg {
        const val MIN_X = 0.0
        const val MAX_X = 3.0

        const val MIN_ECG_Y = -150.0
        const val MAX_ECG_Y = 150.0

        const val MIN_DIFF1_Y = -500.0
        const val MAX_DIFF1_Y = 500.0

        const val MIN_DIFF2_Y = -500.0
        const val MAX_DIFF2_Y = 500.0
    }

    object SimulatedEcg {
        const val MIN_X = 0.0
        const val MAX_X = 10.0

        const val MIN_ECG_Y = 0.0
        const val MAX_ECG_Y = 900.0

        const val MIN_DIFF1_Y = -500.0
        const val MAX_DIFF1_Y = 500.0

        const val MIN_DIFF2_Y = -500.0
        const val MAX_DIFF2_Y = 500.0

        const val MIN_RR_Y = 0.0
        const val MAX_RR_Y = 2.5
    }
}