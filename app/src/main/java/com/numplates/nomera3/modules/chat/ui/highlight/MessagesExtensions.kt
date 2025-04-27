package com.numplates.nomera3.modules.chat.ui.highlight

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.doDelayed

private const val DISABLE_SCROLL_DELAY_MS = 1000L
private const val PROGRAMMATICALLY_SCROLLED = 0
private const val HALF_SIZE = 0.5f

/**
 * Scrolls the RecyclerView to the specified position and highlights the message with a temporary animation.
 *
 * This function adds a scroll listener to the RecyclerView that detects when the message at the specified position
 * is scrolled into view (presumably programmatically). It then applies an animation drawable as a background to the
 * message item, highlighting it temporarily. After a short delay, the background is removed, the animation stops,
 * and the scroll listener is removed.
 *
 * The RecyclerView is then scrolled to the specified position.
 *
 * @param position The adapter position of the message to scroll to and highlight.
 */
fun RecyclerView.scrollWithHighlightingMessage(position: Int) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING,
                RecyclerView.SCROLL_STATE_IDLE -> {
                    removeOnScrollListener(this)
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy == PROGRAMMATICALLY_SCROLLED) {
                if (position == RecyclerView.NO_POSITION || position < 3) {
                    removeOnScrollListener(this)
                    return
                }

                val holder = findViewHolderForAdapterPosition(position) ?: return
                (holder as? Highlightable)?.highlight()

                doDelayed(DISABLE_SCROLL_DELAY_MS) {
                    removeOnScrollListener(this)
                }
            }
        }
    })
    val targetItem = findViewHolderForAdapterPosition(position)
    val targetOffset = height * HALF_SIZE - (targetItem?.itemView?.height ?: 0) * HALF_SIZE
    (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, targetOffset.toInt())
}
