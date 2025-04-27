package com.numplates.nomera3.modules.maps.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.viewpager.widget.ViewPager
import com.numplates.nomera3.modules.maps.ui.snippet.view.PageHolder

class MapSnippetViewPager constructor(
    context: Context,
    attrs: AttributeSet?
) : ViewPager(context, attrs), PageHolder {

    var endOverscrollCallback: (() -> Unit)? = null
    var isPagingEnabled = true
    private var initialTouchX: Float? = null
    private val touchSlop: Int = ViewConfiguration.get(context).scaledPagingTouchSlop

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        checkOverscroll(event)
        return (getCurrentPage() == null || event.y > getActiveTouchY()) && isPagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        checkOverscroll(event)
        return (getCurrentPage() == null || event.y > getActiveTouchY()) &&
            isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        super.setCurrentItem(item, false)
    }

    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, false)
    }

    override fun getCurrentPage(): MapSnippetPage? {
        return findFragment<Fragment>()
            .childFragmentManager
            .fragments
            .mapNotNull { it as? MapSnippetPage }
            .firstOrNull { it.getPageIndex() == currentItem }
    }

    override fun getPage(pageIndex: Int): MapSnippetPage? {
        return findFragment<Fragment>()
            .childFragmentManager
            .fragments
            .mapNotNull { it as? MapSnippetPage }
            .firstOrNull { it.getPageIndex() == pageIndex }
    }

    private fun getActiveTouchY(): Int {
        return height - (getCurrentPage()?.getSnippetHeight() ?: 0)
    }

    private fun checkOverscroll(event: MotionEvent) {
        if (!isPagingEnabled) return
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                initialTouchX?.let { initialX ->
                    if (initialX - x > touchSlop && adapter?.count == currentItem + 1) {
                        endOverscrollCallback?.invoke()
                        initialTouchX = null
                    }
                }
            }
        }
    }
}
