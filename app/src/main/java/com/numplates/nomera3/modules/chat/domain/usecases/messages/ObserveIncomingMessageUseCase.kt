package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIncomingMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    fun invoke(): Flow<MessageEntity> = repository.observeIncomingMessage()

}
