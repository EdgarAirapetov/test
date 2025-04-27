package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class DeleteDialogByIdUseCase @Inject constructor(
    private val roomRepository: RoomDataRepository
){
    fun invoke(roomId: Long) =
        roomRepository.deleteDialogByRoomId(roomId)
}
