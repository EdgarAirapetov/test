package com.numplates.nomera3.modules.maps.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.hitRect

class MapSnippetBackgroundCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    private val pathProvider = ShapeAppearancePathProvider()
    private val path = Path()
    private val rectF = RectF(0f, 0f, 0f, 0f)

    var interceptTouchEvents = true

    init {
        elevation = 0f
    }

    override fun onDraw(canvas: Canvas) {
        pathProvider.calculatePath(shapeAppearanceModel, 1f, rectF, path)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rectF.right = w.toFloat()
        rectF.bottom = h.toFloat()
        pathProvider.calculatePath(shapeAppearanceModel, 1f, rectF, path)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val hitClose = findViewById<View>(R.id.ib_snippet_close)
            ?.hitRect
            ?.contains(ev.x.toInt(), ev.y.toInt())
            ?: false
        return interceptTouchEvents && !hitClose
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
