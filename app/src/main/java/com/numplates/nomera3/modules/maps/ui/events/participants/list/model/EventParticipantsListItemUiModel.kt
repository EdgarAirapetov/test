package com.numplates.nomera3.modules.maps.ui.events.participants.list.model

data class EventParticipantsListItemUiModel(
    val userId: Long,
    val name: String,
    val uniqueName: String,
    val ageLocation: String,
    val avatarUrl: String,
    val isHost: Boolean,
    val isMe: Boolean,
    val isFriend: Boolean,
    val isSubscribed: Boolean,
)
