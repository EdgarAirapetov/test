package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEventMessagesUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    fun invoke(roomId: Long): Flow<List<MessageEntity>> {
        return repository.observeEventMessages(roomId)
    }

}
