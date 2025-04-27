package com.numplates.nomera3.modules.chat.ui.model

import com.meera.db.models.message.ParsedUniquename

data class MessageContentUiModel(
    val isExists: Boolean,
    val rawText: String?,
    val tagSpan: ParsedUniquename?
)
