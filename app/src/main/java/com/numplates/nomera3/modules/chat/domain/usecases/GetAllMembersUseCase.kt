package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatRepository
import javax.inject.Inject

class GetAllMembersUseCase @Inject constructor(
    private val chatRepository: ChatRepository
){
    fun invoke(roomId: Long?) =
        chatRepository.getAllMembers(roomId)
}
