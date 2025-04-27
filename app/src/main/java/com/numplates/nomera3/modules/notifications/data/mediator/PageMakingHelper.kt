package com.numplates.nomera3.modules.notifications.data.mediator

import androidx.paging.PagedList

const val PAGE_SIZE = 60

fun Int.makePage() =
    Page(PAGE_SIZE, (this) * PAGE_SIZE)

fun preparePagingConfig() =
    PagedList.Config.Builder()
        .setPageSize(PAGE_SIZE)
        .setPrefetchDistance(PAGE_SIZE)
        .setEnablePlaceholders(false)
        .build()

data class Page(
    val limit: Int,
    val offset: Int
)
