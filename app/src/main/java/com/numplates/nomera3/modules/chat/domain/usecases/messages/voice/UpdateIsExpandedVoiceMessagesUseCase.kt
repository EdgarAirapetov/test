package com.numplates.nomera3.modules.chat.domain.usecases.messages.voice

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class UpdateIsExpandedVoiceMessagesUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long?, isExpanded: Boolean): Int {
        return repository.updateIsExpandedVoiceMessages(roomId, isExpanded)
    }

}
