package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity

sealed class MediakeyboardFavoriteEvent {

    object OnPagingInitialized : MediakeyboardFavoriteEvent()
    data class OnNetworkStatusReceived(val isConnected: Boolean) : MediakeyboardFavoriteEvent()

}
