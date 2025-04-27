package com.numplates.nomera3.modules.chat.ui.model

import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerConfig

data class ChatMessageDataUiModel(
    val messageData: MessageUiModel,
    val messageConfig: MessageConfigWrapperUiModel,
    val containerConfig: UiKitMessagesContainerConfig,
)
