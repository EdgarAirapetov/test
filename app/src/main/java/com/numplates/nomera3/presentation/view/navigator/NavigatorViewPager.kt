package com.numplates.nomera3.presentation.view.navigator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager

class NavigatorViewPager : ViewPager {

    private var initialXValue: Float = 0.toFloat()
    private var direction: SwipeDirection? = null
    private var previousBeforeMomentPageTransformer: PageTransformer = NavigatorPageTransformerHorizontal()
    private var currentPageTransformer: PageTransformer = NavigatorPageTransformerHorizontal()
    init {
        init()
    }

    enum class SwipeDirection {
        ALL, LEFT, RIGHT, NONE
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun canScrollHorizontally(direction: Int): Boolean {
        return true
    }

    fun lockSwipe() = setAllowedSwipeDirection(SwipeDirection.NONE)

    fun unlockSwipe() = setAllowedSwipeDirection(SwipeDirection.ALL)


    private fun init() {
        setCurrentPageTransformer(false, NavigatorPageTransformerHorizontal())
        setAllowedSwipeDirection(SwipeDirection.NONE)
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    /** Устанавливает продолжительность анимации открытия фрагмента */
    fun setDurationScroll(millis: Int) {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, OwnScroller(context, millis))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPadding(
                insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            insets.replaceSystemWindowInsets(0, insets.systemWindowInsetTop, 0, 0)
        } else {
            super.onApplyWindowInsets(insets)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return if (this.isSwipeAllowed(e)) {
            super.onTouchEvent(e)
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return if (this.isSwipeAllowed(e)) {
            super.onInterceptTouchEvent(e)
        } else {
            false
        }
    }

    fun setCurrentPageTransformer(reverseDrawingOrder: Boolean, pageTransformer: PageTransformer) {
        if (pageTransformer::class.java != currentPageTransformer::class.java) {
            if (currentPageTransformer !is MomentPageTransformer)
                previousBeforeMomentPageTransformer = currentPageTransformer
        }
        setPageTransformer(reverseDrawingOrder, pageTransformer)
        currentPageTransformer = pageTransformer
    }

    fun setPreviousBeforeMomentPageTransformer(reverseDrawingOrder: Boolean) {
        setCurrentPageTransformer(reverseDrawingOrder, previousBeforeMomentPageTransformer)
    }

    fun setAllowedSwipeDirection(direction: SwipeDirection?) {
        this.direction = direction
    }

    private fun isSwipeAllowed(e: MotionEvent?): Boolean {
        if (e == null) return false
        if (this.direction === SwipeDirection.ALL) return true
        if (direction === SwipeDirection.NONE) {
            return false
        }
        if (e.action == MotionEvent.ACTION_DOWN) {
            initialXValue = e.x
            return true
        }
        if (e.action == MotionEvent.ACTION_MOVE) {
            try {
                val diffX = e.x - initialXValue
                if (diffX > 0 && direction === SwipeDirection.RIGHT) {
                    // swipe from left to right detected
                    return false
                } else if (diffX < 0 && direction === SwipeDirection.LEFT) {
                    // swipe from right to left detected
                    return false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        return true
    }

    /**
     * Добавляет интерполятор для замедления открытия фрагмента DecelerateInterpolator()
     */
    inner class OwnScroller(
        context: Context,
        durationScroll: Int
    ) : Scroller(context, DecelerateInterpolator(1.5f)) {

        private var durationScrollMillis = 1

        init {
            this.durationScrollMillis = durationScroll
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, durationScrollMillis)
        }
    }
}
