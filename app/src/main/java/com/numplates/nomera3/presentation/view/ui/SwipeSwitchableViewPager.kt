package com.numplates.nomera3.presentation.view.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class SwipeSwitchableViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    var isSwipeEnabled: Boolean = true
    var unallowedCoords: Rect? = null
    var unallowedCoordsList: ArrayList<Rect>? = null

    var needCheckUnallowedCoords = true

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return isSwipeEnabled && isSwipeAllowed(e) && super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return isSwipeEnabled && isSwipeAllowed(e) && super.onTouchEvent(e)
    }

    override fun executeKeyEvent(event: KeyEvent): Boolean {
        return isSwipeEnabled && super.executeKeyEvent(event)
    }

    private fun isSwipeAllowed(e: MotionEvent?): Boolean {
        val unallowedArea = unallowedCoords
        val unallowedAreas = unallowedCoordsList
        if (e == null || !needCheckUnallowedCoords) return true

        var isSwipeDisallowed = false

        unallowedArea?.let { area ->
            if (area.contains(e.rawX.toInt(), e.rawY.toInt())) isSwipeDisallowed = true
        }

        unallowedAreas?.let { areas ->
            for (area in areas) {
                if (area.contains(e.rawX.toInt(), e.rawY.toInt())) isSwipeDisallowed = true
            }
        }

        return !isSwipeDisallowed
    }
}
