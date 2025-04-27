package com.numplates.nomera3.modules.chat.domain.usecases.tooltips

import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import javax.inject.Inject

class IsLetRecordAudioMessageTooltipUseCase @Inject constructor(
    private val chatPersistRepository: ChatPersistRepository
) {
    fun invoke(times: Int) = chatPersistRepository.getRecordAudioMsgTipShowedTimes() < times
        && chatPersistRepository.isShownTooltipSession()
}
