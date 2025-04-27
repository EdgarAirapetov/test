package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VoiceMessage(
        @SerializedName("data") var voiceData: VoiceData?,
        @SerializedName("link") var link: String?
): Serializable