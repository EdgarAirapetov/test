package com.numplates.nomera3.modules.chat.domain.usecases


import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class GetChatMessageListPositionUsecase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend operator fun invoke(message: MessageEntity): Int = repository.getChatMessageListPosition(message)
}
