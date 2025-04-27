package com.numplates.nomera3.modules.maps.ui.events.participants.list.model

data class EventParticipantsListUiModel(
    val items: List<EventParticipantsListItemUiModel>,
    val participantsCountString: String,
    val isRefreshing: Boolean,
    val isLoadingNextPage: Boolean,
    val isLastPage: Boolean,
    val participantsCount: Int
)
