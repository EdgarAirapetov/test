package com.meera.core.utils

import kotlin.math.cos
import kotlin.math.pow

class BounceInterpolator(
    private var mAmplitude: Double = 1.0,
    private var mFrequency: Double = 10.0
): android.view.animation.Interpolator {

    override fun getInterpolation(input: Float): Float {
        return  (-1 * Math.E.pow(-input / mAmplitude) *
                cos(mFrequency * input) + 1).toFloat()
    }
}