package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class GetLastMessageUpdatedTimeUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long): Long = repository.getLastMessageUpdatedTime(roomId)

}
