package com.numplates.nomera3.modules.chat.drafts.domain.entity

import com.meera.db.models.message.MessageEntity

data class DraftModel(
    val roomId: Long?,
    val userId: Long?,
    val lastUpdatedTimestamp: Long,
    val text: String?,
    val reply: MessageEntity?,
    val draftId: Int? = null
)
