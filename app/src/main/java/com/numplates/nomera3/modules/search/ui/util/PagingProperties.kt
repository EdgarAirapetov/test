package com.numplates.nomera3.modules.search.ui.util

data class PagingProperties(
    var isLastPage: Boolean = false,
    var isLoading: Boolean = false,
    var offset: Int = 0
) {
    fun reset() {
        offset = 0
        isLastPage = false
        isLoading = false
    }
}
