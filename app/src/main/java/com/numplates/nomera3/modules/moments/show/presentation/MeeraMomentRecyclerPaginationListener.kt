package com.numplates.nomera3.modules.moments.show.presentation

import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.moments.show.presentation.view.carousel.MeeraMomentsItemRecyclerView

class MeeraMomentRecyclerPaginationListener(
    private val momentCarousel: MeeraMomentsItemRecyclerView,
    private val momentCallback: MomentCallback
) : RecyclerView.OnScrollListener() {

    private val layoutManager = momentCarousel.layoutManager

    companion object {
        const val PAGE_SIZE = 10
        const val BUFFER_SIZE = 5
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager?.childCount ?: return
        val totalItemCount = layoutManager.itemCount

        val firstItemVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage() && isMomentPagingListTicketValid(momentCarousel.currentPagingTicket)) {
            if ((visibleItemCount + firstItemVisiblePosition + BUFFER_SIZE) >= totalItemCount
                && firstItemVisiblePosition >= 0
                && totalItemCount >= PAGE_SIZE
            ) {
                loadMoreItems(momentCarousel.currentPagingTicket)
            }
        }
    }

    private fun loadMoreItems(pagingTicket: String?) = momentCallback.requestNewMomentsPage(pagingTicket)

    private fun isLastPage(): Boolean = momentCallback.isMomentsCarouselLastPage()

    private fun isLoading(): Boolean = momentCallback.isRequestingMoments()

    private fun isMomentPagingListTicketValid(ticket: String?): Boolean = momentCallback.isMomentPagingListTicketValid(ticket)

}
