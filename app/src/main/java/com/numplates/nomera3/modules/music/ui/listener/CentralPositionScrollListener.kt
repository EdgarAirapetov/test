package com.numplates.nomera3.modules.music.ui.listener

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class CentralPositionScrollListener : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val lm = recyclerView.layoutManager as? LinearLayoutManager?
        onMoveCentralItem(getRange(lm))
    }

    abstract fun onMoveCentralItem(range: IntRange)

    companion object {

        fun getRange(lm: LinearLayoutManager?): IntRange {
            val firstVisibleItemPosition = lm?.findFirstVisibleItemPosition() ?: 0
            val lastVisibleItemPosition = lm?.findLastVisibleItemPosition() ?: 0
            return IntRange(firstVisibleItemPosition, lastVisibleItemPosition)
        }

    }
}
