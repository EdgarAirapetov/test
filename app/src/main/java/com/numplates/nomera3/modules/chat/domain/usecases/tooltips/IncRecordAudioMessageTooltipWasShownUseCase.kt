package com.numplates.nomera3.modules.chat.domain.usecases.tooltips

import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import javax.inject.Inject

class IncRecordAudioMessageTooltipWasShownUseCase @Inject constructor(
    private val chatPersistRepository: ChatPersistRepository
){
    fun invoke(times: Int) {
        val shownTimes = chatPersistRepository.getRecordAudioMsgTipShowedTimes()
        if (shownTimes > times) return
        chatPersistRepository.setRecordAudioMsgTipWasShowed(shownTimes)
    }
}
