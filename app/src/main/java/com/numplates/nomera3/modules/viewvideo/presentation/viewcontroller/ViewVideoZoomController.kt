package com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller

import android.animation.ValueAnimator
import com.google.android.exoplayer2.ui.PlayerView

private const val MIN_ZOOM_SCALE = 1f
private const val MAX_ZOOM_SCALE = 100f

class ViewVideoZoomController(
    private val playerView: PlayerView
) {

    private val scaleBackAnimationDuration =
        playerView.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    private var scaleAnimator: ValueAnimator? = null

    private var startScale = -1f
    private var startY = 0f
    private var startX = 0f
    private var isScaleActive = false

    fun onScaleStart(focusX: Float, focusY: Float) {
        if (isScaleActive) return
        isScaleActive = true
        playerView.apply {
            scaleAnimator?.cancel()
            saveStartingValues()
            pivotX = focusX
            pivotY = focusY
        }
    }

    fun onScale(scale: Float) {
        playerView.apply {
            scaleX = getRangedScale(startScale + (scale - 1))
            scaleY = getRangedScale(startScale + (scale - 1))
            y = startY * (startScale + (getRangedScale(scale) - 1))
            x = startX * (startScale + (getRangedScale(scale) - 1))
        }
    }

    fun onScaleEnd() {
        isScaleActive = false
        playerView.apply {
            scaleAnimator = getZoomBackAnimator()
            scaleAnimator?.start()
        }
    }

    private fun getRangedScale(scale: Float): Float {
        return scale.coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
    }

    private fun saveStartingValues() {
        if (startScale > 0) return
        startScale = playerView.scaleX
        startY = playerView.y
        startX = playerView.x
    }

    private fun getZoomBackAnimator(): ValueAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = scaleBackAnimationDuration
        val scaleDiff = playerView.scaleX - 1f
        val defaultPivotX = (playerView.width / 2f)
        val defaultPivotY = (playerView.height / 2f)
        val pivotDiffX = playerView.pivotX - defaultPivotX
        val pivotDiffY = playerView.pivotY - defaultPivotY
        val diffY = playerView.y - startY
        val diffX = playerView.x - startX
        addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Float
            playerView.apply {
                val animatedScale = 1f + scaleDiff * animatedValue
                scaleX = animatedScale
                scaleY = animatedScale
                pivotX = defaultPivotX + pivotDiffX * animatedValue
                pivotY = defaultPivotY + pivotDiffY * animatedValue
                y = startY + diffY * animatedValue
                x = startX + diffX * animatedValue
            }
        }
    }
}
