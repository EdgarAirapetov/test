package com.numplates.nomera3.modules.chat.domain.usecases.tooltips

import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import javax.inject.Inject

class ShouldShowPhoneAbilityTooltipUseCase @Inject constructor(
    private val chatPersistRepository: ChatPersistRepository
) {
    fun invoke(times: Int): Boolean {
        val showTimes = chatPersistRepository.getPhoneAbilityShowedTimes()
        if (showTimes >= times) return false
        chatPersistRepository.setPhoneAbilityTipWasShowed(showTimes)
        return true
    }
}
