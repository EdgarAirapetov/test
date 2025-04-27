package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentsIndicatorParams


class MomentsCircleIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val STROKE_THICKNESS = 2.dp
    private val GRADIENT_ANGLE = -45f

    private var params: MomentsIndicatorParams? = null
    private var paint: Paint? = null

    private val transparentColor = ContextCompat.getColor(context, R.color.transparent)

    private val viewedMomentsGradientColors = listOf(
        ContextCompat.getColor(context, R.color.moments_viewed_edge_gradient),
        ContextCompat.getColor(context, R.color.moments_viewed_center_gradient),
        ContextCompat.getColor(context, R.color.moments_viewed_edge_gradient)
    ).toIntArray()

    private val momentsGradientColors = listOf(
        ContextCompat.getColor(context, R.color.moments_gradient_start),
        ContextCompat.getColor(context, R.color.moments_gradient_center),
        ContextCompat.getColor(context, R.color.moments_gradient_end)
    ).toIntArray()

    fun bind(params: MomentsIndicatorParams) {
        if (this.params == null) {
            this.params = params
            return
        }

        if (this.params == params) return

        this.params = params
        setupPaint()
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setupPaint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawIndicator(canvas)
    }

    private fun drawIndicator(canvas: Canvas) {
        paint?.let { canvas.drawCircle(width / 2.0f, height / 2.0f, (width - STROKE_THICKNESS) / 2.0f, it) }
    }

    private fun setupPaint() {
        val params = params ?: return

        paint?.reset()

        if (paint == null) paint = Paint()

        paint?.apply {
            style = Paint.Style.STROKE
            strokeWidth = STROKE_THICKNESS.toFloat()
            isAntiAlias = true

            if (params.hasMoments) {
                val shader = if (params.hasNewMoments) {
                    getGradientShader(momentsGradientColors)
                } else {
                    getGradientShader(viewedMomentsGradientColors)
                }
                this.shader = shader
            } else {
                color = transparentColor
            }
        }
    }

    private fun getGradientShader(gradientColors: IntArray): Shader {
        val shader: Shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
            gradientColors, null, TileMode.MIRROR)
        val gradientMatrix = Matrix()
        gradientMatrix.preRotate(GRADIENT_ANGLE, width / 2.0f, height / 2.0f)
        shader.setLocalMatrix(gradientMatrix)

        return shader
    }
}
