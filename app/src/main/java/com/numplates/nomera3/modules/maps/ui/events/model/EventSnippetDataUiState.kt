package com.numplates.nomera3.modules.maps.ui.events.model

sealed interface EventSnippetDataUiState {
    data class PreloadedSnippet(val item: EventSnippetItem.EventPostItem, val isAuxSnippet: Boolean) : EventSnippetDataUiState
    data class Error(val item: EventSnippetItem.ErrorItem) : EventSnippetDataUiState
    data class SnippetList(val items: List<EventSnippetItem>) : EventSnippetDataUiState
    object Empty : EventSnippetDataUiState
}

