package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import javax.inject.Inject

class SaveMessageIntoDbUseCase @Inject constructor(
    private val repository: ChatMessageRepositoryImpl
) {
    suspend fun invoke(
        message: MessageEntity,
        shouldEmmitEvent: Boolean = false
    ): Long {
        return repository.saveMessageIntoDb(
            message = message,
            shouldEmmitEvent = shouldEmmitEvent
        )
    }
}
