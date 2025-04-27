package com.meera.core.utils.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper class for 2 side recyclerview pagination
 * For normal (not reversed) recyclerview
 */
abstract class RecyclerPaginationUtil(
    private val layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount

        val firstItemVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoadingAfter() && !isBottomPage()) {
            if ((visibleItemCount + firstItemVisiblePosition) >= totalItemCount - 10
                && firstItemVisiblePosition >= 0
            ) {
                recyclerView.post { loadAfter() }
            }
        }
    }

    /**
     * Исправляет баг подгрузки комментариев при инициализации списка комментариев.
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val firstItemVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val delta = recyclerView.bottom - recyclerView.height + recyclerView.scrollY
        if (delta == 0) {
            if (!isLoadingBefore() && !isTopPage()) {
                if (firstItemVisiblePosition in 0..10) {
                    recyclerView.post { loadBefore() }
                }
            }
        }
    }

    abstract fun loadBefore()

    abstract fun loadAfter()

    abstract fun isTopPage(): Boolean

    abstract fun isBottomPage(): Boolean

    abstract fun isLoadingAfter(): Boolean

    abstract fun isLoadingBefore(): Boolean

}
