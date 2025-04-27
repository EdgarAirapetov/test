package com.numplates.nomera3.modules.chat.helpers.sendmessage.models

data class VoiceMessageDataModel(
    val audioPath: String?,
    val amplitudes: List<Int>?,
    val durationSec: Long?
)
