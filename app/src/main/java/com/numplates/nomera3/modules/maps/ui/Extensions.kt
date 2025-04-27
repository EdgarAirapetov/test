package com.numplates.nomera3.modules.maps.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlin.math.absoluteValue

const val DEFAULT_LAT_LNG_FLOAT_TOLERANCE = 0.0001
const val MARKER_ANIMATION_DURATION = 500L

fun LatLng.equalWithTolerance(other: LatLng, tolerance: Double = DEFAULT_LAT_LNG_FLOAT_TOLERANCE) =
    this.latitude.minus(other.latitude).absoluteValue <= tolerance
        && this.longitude.minus(other.longitude).absoluteValue <= tolerance

fun Marker.animate() {
    ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        .apply {
            interpolator = LinearInterpolator()
            duration = MARKER_ANIMATION_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(p0: Animator) {
                    this@animate.alpha = 1f
                }

                override fun onAnimationCancel(p0: Animator) {
                    this@animate.alpha = 1f
                }

                override fun onAnimationStart(p0: Animator) {
                    this@animate.alpha = 0f
                }
            })
        }
        .start()
}
