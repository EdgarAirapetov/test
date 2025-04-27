package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import javax.inject.Inject

class UpdateMessageSentStatusUseCase @Inject constructor(
    private val repository: ChatMessageRepositoryImpl
) {

    fun invoke(
        messageId: String,
        isSent: Boolean,
        isShowLoadingProgress: Boolean = false
    ) {
        return repository.updateMessageSentStatus(
            messageId = messageId,
            isSent = isSent,
            isShowLoadingProgress = isShowLoadingProgress
        )
    }

}
