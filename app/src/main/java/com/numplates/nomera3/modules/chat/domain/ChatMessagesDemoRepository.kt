package com.numplates.nomera3.modules.chat.domain

import com.meera.db.models.message.MessageEntity

interface ChatMessagesDemoRepository {

    suspend fun readMessages(): List<MessageEntity>

    suspend fun getMessageById(messageId: String): MessageEntity?
}
