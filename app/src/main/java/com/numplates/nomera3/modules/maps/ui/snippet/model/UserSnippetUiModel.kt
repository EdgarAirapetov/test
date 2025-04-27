package com.numplates.nomera3.modules.maps.ui.snippet.model

data class UserSnippetUiModel(
    val contentState: ContentState,
    val items: List<UserSnippetItem>,
    val showFadeIn: Boolean,
    val selectedUserIsVip: Boolean?,
    val expandedStateRestricted: Boolean,
    val isFull: Boolean = false
)
