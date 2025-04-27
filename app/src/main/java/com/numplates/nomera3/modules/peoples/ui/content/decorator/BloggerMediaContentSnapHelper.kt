package com.numplates.nomera3.modules.peoples.ui.content.decorator

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setBloggerMediaSnapHelper() {
    object : PagerSnapHelper() {
        override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
            val linearLayoutManager =
                (layoutManager as? LinearLayoutManager) ?: return super.findSnapView(layoutManager)
            val firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
            val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
            val firstItem = 0
            val lastItem = layoutManager.itemCount - 1
            return when {
                firstItem == firstVisiblePosition -> layoutManager.findViewByPosition(firstVisiblePosition)
                lastItem == lastVisiblePosition -> layoutManager.findViewByPosition(lastVisiblePosition)
                else -> super.findSnapView(layoutManager)
            }
        }
    }.apply { attachToRecyclerView(this@setBloggerMediaSnapHelper) }
}
