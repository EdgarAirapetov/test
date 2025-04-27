package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.chat.toolbar.data.entity.ChatUserTypingEntity
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatUserTypingObserverUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) {

    fun observe(): Flow<ChatUserTypingEntity> {
        return repository.observeTyping()
    }

}

