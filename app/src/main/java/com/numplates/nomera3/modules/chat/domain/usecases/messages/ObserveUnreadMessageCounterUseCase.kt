package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUnreadMessageCounterUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    fun invoke(roomId: Long?): Flow<Long?> {
        return repository.observeUnreadMessageCounter(roomId)
    }

}
