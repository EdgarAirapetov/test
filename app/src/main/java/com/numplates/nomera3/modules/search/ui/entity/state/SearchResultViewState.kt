package com.numplates.nomera3.modules.search.ui.entity.state

import com.numplates.nomera3.modules.search.ui.entity.SearchItem

sealed class SearchResultViewState {
    open class Data(open val value: List<SearchItem>) : SearchResultViewState()

    object SearchStart : Data(emptyList())
    data class DefaultResult(override val value: List<SearchItem>) : Data(value)
    data class SearchResult(
        override val value: List<SearchItem>,
        val needToLogSearch: Boolean = true
    ) : Data(value)
    data class UpdateSearchResultItem(
        override val value: List<SearchItem>,
        val updatedUser: SearchItem.User
    ) : Data(value)
}
