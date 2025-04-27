package com.numplates.nomera3.modules.maps.ui.model

sealed interface FocusedMapItem {
    data class User(val userId: Long) : FocusedMapItem
    data class Event(val eventObject: EventObjectUiModel) : FocusedMapItem
}

fun FocusedMapItem?.userId(): Long? = (this as? FocusedMapItem.User)?.userId

fun FocusedMapItem?.eventPostId(): Long? = (this as? FocusedMapItem.Event)?.eventObject?.eventPost?.postId
