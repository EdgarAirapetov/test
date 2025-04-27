package com.numplates.nomera3.modules.moments.show.presentation.custom

import android.view.View

private const val TRANSFORM_DISTANCE_MULTIPLIER = 20f
private const val TRANSFORM_ROTATION_ANGLE = 90f

class CubeTransform : BaseTransform() {

    public override val isPagingEnabled: Boolean
        get() = true

    override fun onTransform(page: View, position: Float) {
        page.apply {
            cameraDistance = width * TRANSFORM_DISTANCE_MULTIPLIER
            pivotX = if (position < 0f) width.toFloat() else TRANSFORM_DEFAULT_PIVOT
            pivotY = height * 0.5f
            rotationY = TRANSFORM_ROTATION_ANGLE * position
        }
    }
}
