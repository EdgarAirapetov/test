package com.numplates.nomera3.modules.chat.helpers.demo

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessagesDemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DemoMessagesProvider @Inject constructor(
    private val repository: ChatMessagesDemoRepository
) {

    fun provideMessagesFlow(): Flow<List<MessageEntity>> {
        return flow { emit(repository.readMessages()) }
    }
}
