package com.meera.core.extensions.view

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed

fun RecyclerView.addOnScrollWithBottomSheetListener(behaviour: BottomSheetBehavior<View>, delay: Long) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (recyclerView.findFirstCompletelyVisibleItemPosition() != 0) behaviour.isDraggable = false
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                doDelayed(delay) {
                    behaviour.isDraggable = true
                }
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if (recyclerView.findFirstCompletelyVisibleItemPosition() != 0) behaviour.isDraggable = false
            }
        }

        private fun RecyclerView.findFirstCompletelyVisibleItemPosition(): Int? {
            val layoutManager = layoutManager as? LinearLayoutManager?
            return layoutManager?.findFirstCompletelyVisibleItemPosition()
        }
    })
}

fun RecyclerView.addOnScrollStateDragging(action: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                action()
            }
        }
    })
}
