package com.numplates.nomera3.modules.chatrooms.ui.gestures

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx

private const val SLIDE_BUTTON_WIDTH = 39 // TODO: во 2й итерации вернуть кнопку "выкл звук". 78 - старое значение

fun RecyclerView.resetSwipedItems() {
    val adapter = requireNotNull(adapter)
    for (i in adapter.itemCount downTo 0) {
        val itemView = findViewHolderForAdapterPosition(i)?.itemView
        if (itemView != null && itemView.scrollX > 0) {
            itemView.scrollTo(0, 0)
        }
    }
}

interface SwipeViewHolder {
    fun canSwipe(): Boolean
}

fun setItemTouchHelper(
    recyclerView: RecyclerView,
    buttonsLimit: Int,
    adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    onSwipeComplete: () -> Unit = {}
) {

    ItemTouchHelper(object : ItemTouchHelper.Callback() {

        private val limitScrollX = buttonsLimit * dpToPx(SLIDE_BUTTON_WIDTH)
        private var currentScrollX = 0
        private var currentScrollXWhenInActive = 0
        private var initXWhenInActive = 0f
        private var firstInActive = false
        private var leftSwipeChecker = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            if (viewHolder is SwipeViewHolder && viewHolder.canSwipe()) {
                val dragFlags = 0
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipeFlags)
            } else {
                return ItemTouchHelper.ACTION_STATE_IDLE
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return Integer.MAX_VALUE.toFloat()
        }

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
            return Integer.MAX_VALUE.toFloat()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                if (viewHolder.itemView.scrollX == 0) {
                    leftSwipeChecker = true
                }

                leftSwipeChecker = leftSwipeChecker && dX < 0

                if (leftSwipeChecker) {
                    recoverSwipedItem(viewHolder, recyclerView)
                    if (viewHolder.itemView.scrollX != 0) {
                        onSwipeComplete.invoke()
                        leftSwipeChecker = false
                    }
                }

                if (dX == 0f) {
                    currentScrollX = viewHolder.itemView.scrollX
                    firstInActive = true
                }

                if (isCurrentlyActive) {
                    var scrollOffset = currentScrollX + (-dX).toInt()
                    if (scrollOffset > limitScrollX) {
                        scrollOffset = limitScrollX
                    } else if (scrollOffset < 0) {
                        scrollOffset = 0
                    }
                    viewHolder.itemView.scrollTo(scrollOffset, 0)
                } else {
                    if (firstInActive) {
                        firstInActive = false
                        currentScrollXWhenInActive = viewHolder.itemView.scrollX
                        initXWhenInActive = dX
                    }

                    if (viewHolder.itemView.scrollX < limitScrollX) {
                        viewHolder.itemView.scrollTo(
                            (currentScrollXWhenInActive * dX / initXWhenInActive).toInt(),
                            0
                        )
                    }
                }
            }
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder.itemView.scrollX > limitScrollX) {
                viewHolder.itemView.scrollTo(limitScrollX, 0)
            } else if (viewHolder.itemView.scrollX < 0) {
                viewHolder.itemView.scrollTo(0, 0)
            }
        }

        private fun recoverSwipedItem(
            viewHolder: RecyclerView.ViewHolder,
            recyclerView: RecyclerView
        ) {

            for (i in adapter.itemCount downTo 0) {
                val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView

                if (i != viewHolder.absoluteAdapterPosition) {

                    itemView?.let {
                        if (it.scrollX > 0) {
                            recoverItemAnim(itemView)
                        }
                    }
                }

                itemView?.setOnClickListener {
                    recoverItemAnim(itemView)
                }
            }
        }

        private fun recoverItemAnim(itemView: View?) {
            itemView?.scrollTo(0, 0)
        }

    }).apply {
        attachToRecyclerView(recyclerView)
    }
}
