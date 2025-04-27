package com.numplates.nomera3.modules.chat.helpers.voicemessage

import com.meera.db.models.message.MessageEntity

interface VoiceMessagePlayerCallback {

    fun onDownloadVoiceMessage(message: MessageEntity)

    fun keepScreen(isEnable: Boolean)

}
