package com.meera.core.utils.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val DEFAULT_PAGE_SIZE = 30
private const val DEFAULT_BUFFER_SIZE = 15

abstract class RecyclerPaginationListener(
    private val layoutManager: LinearLayoutManager,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : RecyclerView.OnScrollListener() {

    private var firstItemVisiblePosition: Int? = null

    abstract fun loadMoreItems()

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (isLoading() || isLastPage()) return
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val newFirstItemVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (visibleItemCount + newFirstItemVisiblePosition + bufferSize >= totalItemCount &&
            newFirstItemVisiblePosition >= 0 && totalItemCount >= pageSize) {
            if (isLoadNextPageAllowed(newFirstItemVisiblePosition)) return
            this.firstItemVisiblePosition = newFirstItemVisiblePosition
            loadMoreItems()
        }
    }

    fun release() {
        this.firstItemVisiblePosition = null
    }

    private fun isLoadNextPageAllowed(newFirstItemVisiblePosition: Int): Boolean {
        val range = newFirstItemVisiblePosition - bufferSize .. newFirstItemVisiblePosition + bufferSize
        return range.contains(firstItemVisiblePosition)
    }
}

