package com.numplates.nomera3.modules.maps.ui.snippet.view

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

object BottomSheetVPHelper {

    fun setupViewPager(vp: ViewPager, nestedPageScrollingEnabled: Boolean) {
        findBottomSheetParent(vp)?.let {
            vp.addOnPageChangeListener(
                BottomSheetViewPagerListener(
                    vp = vp,
                    behavior = ViewPagerBottomSheetBehavior.from(it),
                    nestedPageScrollingEnabled = nestedPageScrollingEnabled
                )
            )
        }
    }

    fun setNestedPageScrollingEnabled(enabled: Boolean, vp: ViewPager) {
        findBottomSheetParent(vp)?.let {
            val behavior = ViewPagerBottomSheetBehavior.from(it)
            vp.post {
                behavior.invalidateScrollingChild(enabled)
            }
        }
    }

    fun setNestedPageScrollingEnabled(enabled: Boolean, vp: ViewPager2) {
        vp.children.find { it is RecyclerView }?.let {
            (it as RecyclerView).isNestedScrollingEnabled = enabled
        }
    }

    @JvmStatic
    fun getCurrentViewWithVP(vp: ViewPager): View? {
        val currentItem: Int = vp.currentItem
        (0 until vp.childCount).forEach { index ->
            val child = vp.getChildAt(index)
            val layoutParams = child.layoutParams as ViewPager.LayoutParams
            val position = runCatching {
                val positionField = layoutParams.javaClass.getDeclaredField("position")
                positionField.isAccessible = true
                positionField.get(layoutParams) as Int
            }.getOrElse { -1 }
            if (layoutParams.isDecor.not() && currentItem == position) {
                return child
            }
        }
        return null
    }

    private fun findBottomSheetParent(view: View): View? {
        var current: View? = view
        while (current != null) {
            val params = current.layoutParams
            if (params is CoordinatorLayout.LayoutParams && params.behavior is ViewPagerBottomSheetBehavior<*>) {
                return current
            }
            val parent = current.parent
            current = if (parent !is View) null else parent
        }
        return null
    }

    private class BottomSheetViewPagerListener(
        private val vp: ViewPager,
        private val behavior: ViewPagerBottomSheetBehavior<View>,
        private val nestedPageScrollingEnabled: Boolean
    ) : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            vp.post {
                behavior.invalidateScrollingChild(nestedPageScrollingEnabled)
            }
        }

    }
}
