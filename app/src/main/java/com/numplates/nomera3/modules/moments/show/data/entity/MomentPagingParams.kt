package com.numplates.nomera3.modules.moments.show.data.entity

import com.numplates.nomera3.modules.moments.show.domain.DEFAULT_MOMENTS_PAGE_LIMIT

data class MomentPagingParams(
    val sessionId: String? = null,
    val newLoad: Boolean = true,
    val startId: Int = 0,
    val isLastPage: Boolean = false,
    val limit: Int = DEFAULT_MOMENTS_PAGE_LIMIT,
    val lastConsumedPagingTicket: String? = null,
    val lastProducedPagingTicket: String? = null
) {
    companion object {
        const val FORBID_PAGING_TICKET = "FORBID_PAGING_TICKET"
    }
}
