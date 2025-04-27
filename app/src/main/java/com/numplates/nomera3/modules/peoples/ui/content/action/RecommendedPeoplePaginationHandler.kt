package com.numplates.nomera3.modules.peoples.ui.content.action

interface RecommendedPeoplePaginationHandler {
    fun loadMore(offsetCount: Int, rootAdapterPosition: Int)
    fun onLast(): Boolean
    fun isLoading(): Boolean
}
