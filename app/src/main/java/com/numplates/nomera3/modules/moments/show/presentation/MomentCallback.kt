package com.numplates.nomera3.modules.moments.show.presentation

import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint

interface MomentCallback {

    /**
     * Request a new moments carousel page
     */
    fun requestNewMomentsPage(pagingTicket: String?)

    /**
     * Moments Carousel is detached or hidden in any way(not visible to user)
     * and we can perform a sorting operation
     */
    fun onMomentsCarouselBecomeNotVisible()

    /**
     * Is next Moments Carousel page being requested right now
     */
    fun isRequestingMoments(): Boolean

    /**
     * Is the last page of the Moments Carousel
     */
    fun isMomentsCarouselLastPage(): Boolean

    /**
     * Check if the ticket with which the paging request is being made is valid
     *
     * We do this to avoid calling [requestNewMomentsPage] in the short period
     * after [isRequestingMoments] == false but a new list is
     * in the process of being submitted to the adapter
     *
     * The risk of this happening is increased since we have to
     * first submit data to the [FeedAdapter],
     * and then submit that data to the [MomentItemAdapter],
     * and both have async behavior during updating
     */
    fun isMomentPagingListTicketValid(ticket: String?): Boolean

    /**
     * Data about create moment tap for analytics
     */
    fun onMomentTapCreate(entryPoint: AmplitudePropertyMomentEntryPoint)

    fun isMomentsScroll(value: Boolean) = Unit

}
