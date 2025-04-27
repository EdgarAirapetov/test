package com.numplates.nomera3.modules.newroads.util

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Исправляет баг в методе smoothScrollToPosition класса
 * LinearLayoutManager, при ввызове smoothScrollToPosition вверху списка.
 */
open class FixedScrollingLinearLayoutManager(
    context: Context?
) : LinearLayoutManager(context) {

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val child = getChildAt(position)
        if (child?.top == 0) {
            super.scrollToPosition(position)
        } else {
            super.smoothScrollToPosition(recyclerView, state, position)
        }
    }
}