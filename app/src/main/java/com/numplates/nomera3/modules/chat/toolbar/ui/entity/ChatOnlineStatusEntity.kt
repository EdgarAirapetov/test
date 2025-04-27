package com.numplates.nomera3.modules.chat.toolbar.ui.entity

import com.meera.core.extensions.empty

data class ChatOnlineStatusEntity(
    val isShowStatus: Boolean = false,
    val networkStatus: NetworkChatStatus = NetworkChatStatus.OFFLINE,
    val isShowDotIndicator: Boolean = false,
    val message: String? = String.empty()
)
