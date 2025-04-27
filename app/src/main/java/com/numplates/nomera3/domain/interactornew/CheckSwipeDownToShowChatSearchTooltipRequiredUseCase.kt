package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class CheckSwipeDownToShowChatSearchTooltipRequiredUseCase @Inject constructor(
    private val roomDataRepository: RoomDataRepository,
) {

    fun invoke(): Boolean {
        return roomDataRepository.checkSwipeDownToShowChatSearchTooltipRequired()
    }
}
