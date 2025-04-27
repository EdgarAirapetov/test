package com.numplates.nomera3.modules.maps.ui.view

import android.view.View
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState

interface MapSnippetPage {
    fun onSelectPage()
    fun onDestroyPage()

    fun setSnippetState(snippetState: SnippetState.StableSnippetState)
    fun setSnippetHeight(height: Int)

    fun getSnippetState(): SnippetState
    fun getPageIndex(): Int?
    fun getSnippetHeight(): Int
    fun getSnippetView(): View?
}
