package com.numplates.nomera3.modules.chat.helpers.voicemessage

import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel

interface MeeraVoiceMessagePlayerCallback {

    fun onDownloadVoiceMessage(message: MessageUiModel)

    fun keepScreen(isEnable: Boolean)
}
