package com.numplates.nomera3.modules.chat.ui.model

import com.meera.db.models.message.MessageEntity

data class PlayMessageDataUiModel(
    val position: Int,
    val message: MessageEntity
)
