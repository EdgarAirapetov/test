package com.numplates.nomera3.modules.chat.domain.usecases


import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.App.Companion.IS_MOCKED_DATA
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import com.numplates.nomera3.modules.chat.domain.ChatMessagesDemoRepository
import javax.inject.Inject

class GetMessageByIdUseCase @Inject constructor(
    private val demoRepository: ChatMessagesDemoRepository,
    private val repository: ChatMessageRepository
) {

    suspend operator fun invoke(messageId: String): MessageEntity? {
        return if (IS_MOCKED_DATA) {
            demoRepository.getMessageById(messageId)
        } else {
            repository.getMessageById(messageId)
        }
    }
}
