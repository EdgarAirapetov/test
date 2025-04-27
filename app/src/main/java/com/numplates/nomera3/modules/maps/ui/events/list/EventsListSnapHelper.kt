package com.numplates.nomera3.modules.maps.ui.events.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

class EventsListSnapHelper : LinearSnapHelper() {
    private var verticalHelper: OrientationHelper? = null
    private var horizontalHelper: OrientationHelper? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.onFlingListener = null
        super.attachToRecyclerView(recyclerView)
    }

    fun detachSnapHelper(){
        super.attachToRecyclerView(null)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return if (layoutManager is LinearLayoutManager) {
            if (layoutManager.canScrollHorizontally()) {
                getStartView(layoutManager, getHorizontalHelper(layoutManager))
            } else {
                getStartView(layoutManager, getVerticalHelper(layoutManager))
            }
        } else {
            super.findSnapView(layoutManager)
        }
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int =
        (layoutManager as? RecyclerView.SmoothScroller.ScrollVectorProvider)
            ?.let { findSnapView(layoutManager) }
            ?.let { currentView -> layoutManager.getPosition(currentView) }
            ?: RecyclerView.NO_POSITION

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun getStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()
            val isLastItem = (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1)
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }
            val child = layoutManager.findViewByPosition(firstChild)
            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                && helper.getDecoratedEnd(child) > 0) {
                child
            } else {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                    null
                } else {
                    layoutManager.findViewByPosition(firstChild + 1)
                }
            }
        }
        return super.findSnapView(layoutManager)
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper =
        verticalHelper ?: OrientationHelper.createVerticalHelper(layoutManager).apply { verticalHelper = this }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper =
        horizontalHelper ?: OrientationHelper.createHorizontalHelper(layoutManager).apply { horizontalHelper = this }
}
