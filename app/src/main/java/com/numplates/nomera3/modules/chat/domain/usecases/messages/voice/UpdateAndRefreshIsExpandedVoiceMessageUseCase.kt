package com.numplates.nomera3.modules.chat.domain.usecases.messages.voice

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class UpdateAndRefreshIsExpandedVoiceMessageUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(messageId: String?, isExpanded: Boolean?): Int {
        return repository.updateAndRefreshIsExpandedVoiceMessage(messageId, isExpanded)
    }

}
