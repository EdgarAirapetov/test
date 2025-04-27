package com.numplates.nomera3.modules.chat.helpers.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatPaginator(
    private val chatRecyclerView: RecyclerView,
    private val layoutManager: LinearLayoutManager,
    private val paginationCallback: PaginationCallback
) {

    init {
        initPagination()
    }

    private fun initPagination() {
        chatRecyclerView.addOnScrollListener(
            RecyclerPaginationUtil(layoutManager, paginationCallback)
        )
    }
}

class RecyclerPaginationUtil(
    private val layoutManager: LinearLayoutManager,
    private val paginationCallback: PaginationCallback
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount

        val firstItemVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (!paginationCallback.isLoadingBefore() && !paginationCallback.isTopPage()) {
            if ((visibleItemCount + firstItemVisiblePosition) >= totalItemCount - CHAT_PAGINATION_BEFORE_DISTANCE
                && firstItemVisiblePosition >= 0
            ) {
                recyclerView.post { paginationCallback.loadBefore() }
            }
        }

        if (!paginationCallback.isLoadingAfter() && !paginationCallback.isBottomPage()) {
            if (firstItemVisiblePosition in 0..CHAT_PAGINATION_AFTER_DISTANCE) {
                recyclerView.post {
                    paginationCallback.loadAfter()
                }
            }
        }
    }
}

interface PaginationCallback {
    fun loadBefore()
    fun loadAfter()
    fun isTopPage(): Boolean
    fun isBottomPage(): Boolean
    fun isLoadingAfter(): Boolean
    fun isLoadingBefore(): Boolean
}

private const val CHAT_PAGINATION_BEFORE_DISTANCE = 40
private const val CHAT_PAGINATION_AFTER_DISTANCE = 20

