package com.numplates.nomera3.modules.search.ui.viewmodel.base

import kotlinx.coroutines.Job

const val CLEAR_RECENT_DELAY_SEC = 5
const val CLEAR_RECENT_DELAY_MS = CLEAR_RECENT_DELAY_SEC * 1000L

abstract class SearchDefaultScreenBaseViewModel : SearchBaseScreenViewModel() {

    protected var clearRecentJob: Job? = null

    abstract fun reload()

    abstract fun clearRecent(force: Boolean = false)

    fun isClearingRecent(): Boolean {
        return clearRecentJob != null && clearRecentJob?.isActive == true
    }

    open fun undoClearRecent() {
        clearRecentJob?.cancel()
    }

    fun forceClearRecent() {
        clearRecentJob?.cancel()
        clearRecent(true)
    }
}