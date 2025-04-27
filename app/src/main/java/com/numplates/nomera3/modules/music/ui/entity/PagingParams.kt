package com.numplates.nomera3.modules.music.ui.entity

class PagingParams {
    val limit: Int = 20
    var isLastPage: Boolean = false
    var searchText: String = ""

    set(value) {
        if (value != field) clear()
        field = value
    }

    var offset: Int = 0

    fun clear() {
        isLastPage = false
        offset = 0
    }

}