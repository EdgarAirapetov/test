package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.numplates.nomera3.R


/**
 * Custom wrapper view to get round corner round view
 */
class RoundedFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var topLeftCornerRadius = 0f
    private var topRightCornerRadius = 0f
    private var bottomLeftCornerRadius = 0f
    private var bottomRightCornerRadius = 0f

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.RoundedFrameLayout) {
            topLeftCornerRadius = getDimension(R.styleable.RoundedFrameLayout_topLeftCornerRadius, 0f)
            topRightCornerRadius = getDimension(R.styleable.RoundedFrameLayout_topRightCornerRadius, 0f)
            bottomLeftCornerRadius = getDimension(R.styleable.RoundedFrameLayout_bottomLeftCornerRadius, 0f)
            bottomRightCornerRadius = getDimension(R.styleable.RoundedFrameLayout_bottomRightCornerRadius, 0f)
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val count: Int = canvas.save()
        val path = Path()
        val cornerDimensions = floatArrayOf(
            topLeftCornerRadius, topLeftCornerRadius,
            topRightCornerRadius, topRightCornerRadius,
            bottomRightCornerRadius, bottomRightCornerRadius,
            bottomLeftCornerRadius, bottomLeftCornerRadius
        )
        path.addRoundRect(
            RectF(
                0f,
                0f,
                canvas.width.toFloat(),
                canvas.height.toFloat()),
            cornerDimensions,
            Path.Direction.CW
        )
        canvas.clipPath(path, Region.Op.INTERSECT)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(count)
    }

    fun setCornerRadius(cornerRadius: Float): RoundedFrameLayout {
        topLeftCornerRadius = cornerRadius
        topRightCornerRadius = cornerRadius
        bottomLeftCornerRadius = cornerRadius
        bottomRightCornerRadius = cornerRadius
        return this
    }

    fun setTopLeftCornerRadius(topLeftCornerRadius: Float): RoundedFrameLayout {
        this.topLeftCornerRadius = topLeftCornerRadius
        return this
    }

    fun setTopRightCornerRadius(topRightCornerRadius: Float): RoundedFrameLayout {
        this.topRightCornerRadius = topRightCornerRadius
        return this
    }

    fun setBottomLeftCornerRadius(bottomLeftCornerRadius: Float): RoundedFrameLayout {
        this.bottomLeftCornerRadius = bottomLeftCornerRadius
        return this
    }

    fun setBottomRightCornerRadius(bottomRightCornerRadius: Float): RoundedFrameLayout {
        this.bottomRightCornerRadius = bottomRightCornerRadius
        return this
    }

    fun apply() {
        invalidate()
    }
}
