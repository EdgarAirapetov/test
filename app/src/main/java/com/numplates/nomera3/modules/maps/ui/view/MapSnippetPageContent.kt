package com.numplates.nomera3.modules.maps.ui.view

import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState

interface MapSnippetPageContent {
    fun onPageSelected()
    fun onSnippetStateChanged(snippetState: SnippetState)
}
