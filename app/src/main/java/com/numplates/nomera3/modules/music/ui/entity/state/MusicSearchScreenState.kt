package com.numplates.nomera3.modules.music.ui.entity.state

import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity

sealed class MusicSearchScreenState {
    data class RecommendationState(
            val recommendations: List<MusicCellUIEntity>,
            val status: Status,
            val needToScrollUp: Boolean = false
    ) : MusicSearchScreenState()

    data class SearchResultState(
            val searchList: List<MusicCellUIEntity>,
            val status: Status,
            val needToScrollUp: Boolean = false
    ) : MusicSearchScreenState()

    object MusicSearchLoading : MusicSearchScreenState()
}

enum class Status {
    STATUS_OK,
    STATUS_NETWORK_ERROR,
    STATUS_EMPTY_LIST
}
