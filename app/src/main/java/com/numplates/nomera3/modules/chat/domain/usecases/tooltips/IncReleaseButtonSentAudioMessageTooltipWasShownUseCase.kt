package com.numplates.nomera3.modules.chat.domain.usecases.tooltips

import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import javax.inject.Inject

class IncReleaseButtonSentAudioMessageTooltipWasShownUseCase @Inject constructor(
    private val chatPersistRepository: ChatPersistRepository
) {
    fun invoke(times: Int) {
        val shownTimes = chatPersistRepository.getReleaseBtnSentAudioMsgTipWasShowed()
        if (shownTimes > times) return
        chatPersistRepository.setReleaseBtnSentAudioMsgTipWasShowed(shownTimes)
    }
}
