package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class DeleteUnsentMessageUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(message: MessageEntity) {
        repository.deleteUnsentMessage(message)
    }

}
