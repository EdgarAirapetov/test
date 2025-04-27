package com.numplates.nomera3.modules.maps.ui.snippet.view

import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPage

interface PageHolder {
    fun getCurrentPage(): MapSnippetPage?
    fun getPage(pageIndex: Int): MapSnippetPage?
}
