package com.numplates.nomera3.presentation.view.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ScrollingView
import androidx.core.view.children
import androidx.core.view.isInvisible
import com.meera.core.extensions.updatePadding
import com.meera.uikit.widgets.dp
import kotlin.math.min

class MeeraPullToRefreshLayout : ViewGroup {
    private var refreshIndicator: MeeraLoaderView? = null
    private var scrollingView: View? = null
    private var refreshIndicatorHeight = 0
    private var currentOffset = 0
    private var isRefreshing = false
    private var onRefreshListener: OnRefreshListener? = null
    private var onPullOffsetListener: OnPullOffsetListener? = null
    private var startY = 0f
    private var isRefreshEnabled = false
    private var shouldShowLoader = true

    private val LOADER_HEIGHT = 48.dp
    private val LOADER_VERTICAL_PADDING = 8.dp

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        refreshIndicator = MeeraLoaderView(context)
        refreshIndicator?.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LOADER_HEIGHT
            )
            updatePadding(
                paddingTop = LOADER_VERTICAL_PADDING,
                paddingBottom = LOADER_VERTICAL_PADDING
            )
        }
        addView(refreshIndicator)
    }

    fun setShouldShowLoader(show: Boolean) {
        shouldShowLoader = show
        refreshIndicator?.isInvisible = !show
    }

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        onRefreshListener = listener
    }

    fun setOnPullOffsetListener(listener: OnPullOffsetListener) {
        onPullOffsetListener = listener
    }

    fun setRefreshing(refreshing: Boolean) {
        isRefreshing = refreshing
        if (refreshing && shouldShowLoader) {
            refreshIndicator?.show()
        } else {
            refreshIndicator?.hide()
            smoothScrollTo(0)
        }
    }

    fun setRefreshEnable(enable: Boolean) = post {
        isRefreshEnabled = enable
    }

    fun release() {
        refreshIndicator = null
        onRefreshListener = null
        onPullOffsetListener = null
        scrollingView = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChild(refreshIndicator, widthMeasureSpec, heightMeasureSpec)
        refreshIndicatorHeight = refreshIndicator?.measuredHeight ?: 0

        val recyclerWidth = MeasureSpec.getSize(widthMeasureSpec)
        val recyclerHeight = MeasureSpec.getSize(heightMeasureSpec)
        scrollingView?.measure(
            MeasureSpec.makeMeasureSpec(recyclerWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(recyclerHeight, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(recyclerWidth, recyclerHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        refreshIndicator?.layout(
            0, -refreshIndicatorHeight + currentOffset,
            measuredWidth, currentOffset
        )

        scrollingView?.layout(
            0, currentOffset,
            measuredWidth, measuredHeight + currentOffset
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> startY = ev.y
            MotionEvent.ACTION_MOVE -> {
                val dy = ev.y - startY
                if (dy > 0 && !canScrollUp() && isRefreshEnabled) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startY = event.y
            MotionEvent.ACTION_MOVE -> {
                val dy = (event.y - startY).toInt()
                if (dy > 0) {
                    updateOffset(dy / 2)
                }
            }

            MotionEvent.ACTION_UP -> if (currentOffset >= refreshIndicatorHeight) {
                triggerRefresh()
            } else {
                smoothScrollTo(0)
            }
        }
        return true
    }

    private fun updateOffset(offset: Int) {
        currentOffset = min(offset, refreshIndicatorHeight * 2)
        onPullOffsetListener?.onOffsetChanged(currentOffset * 2)
        requestLayout()
    }

    private fun triggerRefresh() {
        isRefreshing = true
        smoothScrollTo(refreshIndicatorHeight)
        if (shouldShowLoader) refreshIndicator?.show()
        onRefreshListener?.onRefresh()
    }

    private fun smoothScrollTo(targetOffset: Int) {
        val animator = ValueAnimator.ofInt(currentOffset, targetOffset)
        animator.apply {
            addUpdateListener { animation: ValueAnimator ->
                currentOffset = animation.getAnimatedValue() as Int
                onPullOffsetListener?.onOffsetChanged(currentOffset * 2)
                requestLayout()
            }
            start()
        }
    }

    private fun canScrollUp(): Boolean {
        return scrollingView?.canScrollVertically(-1) ?: true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        scrollingView = children.firstOrNull { it is ScrollingView }
    }

    interface OnRefreshListener {
        fun onRefresh()
    }

    interface OnPullOffsetListener {
        fun onOffsetChanged(offset: Int)
    }
}
