package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class ChangeMessageEditingStatusUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(messageId: String, isEditing: Boolean) {
        repository.updateMessageEditingStatus(messageId = messageId, isEditing = isEditing)
    }
}
