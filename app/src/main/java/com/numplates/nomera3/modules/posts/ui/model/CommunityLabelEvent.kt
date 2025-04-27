package com.numplates.nomera3.modules.posts.ui.model

sealed interface CommunityLabelEvent {
    data class CommunityClicked(val communityId: Long) : CommunityLabelEvent
}
