package com.numplates.nomera3.modules.search.ui.fragment

import androidx.lifecycle.Lifecycle
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters

interface SearchScreenContext {
    fun setScreenState(state: ScreenState)
    fun getScreenState(): ScreenState?

    fun blankSearch()
    fun clearCurrentResult()
    fun search(query: String)
    fun hideMessages()
    fun exitScreen()
    fun showAndLoadSearchScreen(numberSearchParameters: NumberSearchParameters)
    fun getFragmentLifecycle(): Lifecycle


    enum class ScreenState {
        Default,
        Result
    }
}
