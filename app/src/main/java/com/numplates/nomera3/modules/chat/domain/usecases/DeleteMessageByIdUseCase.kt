package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class DeleteMessageByIdUseCase @Inject constructor(
    private val roomDataRepository: RoomDataRepository
){
    suspend fun invoke(roomId: Long, messageId: String) =
        roomDataRepository.deleteMessageById(roomId, messageId)
}
