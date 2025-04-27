package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.numplates.nomera3.modules.chat.toolbar.data.entity.OnlineChatStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatOnlineStatusObserverUseCase @Inject constructor(
    private val repository: ChatToolbarRepositoryImpl
) {

    fun observe(roomId: Long?): Flow<OnlineChatStatusEntity> =
        repository.observeOnlineStatus(roomId)

}