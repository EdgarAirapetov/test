package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.View

class CircleWaterView constructor(var cx: Float, var cy: Float, context: Context) : View(context) {
    private val baseRadius = 10
    private lateinit var paint: Paint
    private var radius = 0
    private var radius2 = 0
    private var radius3 = 0

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#5c6b48d8")
        paint.isAntiAlias = true
        paint.strokeWidth = 2f
    }

    fun updateView(radius: Int) {
        this.radius = radius

        this.radius2 = if (radius > 200) {
            radius + baseRadius - 200
        } else {
            radius + baseRadius + 400
        }

        this.radius3 = if (radius > 400) {
            radius + baseRadius - 400
        } else {
            radius + baseRadius + 200
        }

        invalidate()
    }

    fun setColor(color: String) {
        paint.color = Color.parseColor(color)
    }

    fun setPos(pos: Point) {
        val newX = pos.x.toFloat()
        if (newX != cx) this.cx = newX

        val newY = pos.y.toFloat()
        if (newY != cy) this.cy = newY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(cx, cy, radius.toFloat(), paint)
        canvas.drawCircle(cx, cy, radius2.toFloat(), paint)
        canvas.drawCircle(cx, cy, radius3.toFloat(), paint)
    }
}
