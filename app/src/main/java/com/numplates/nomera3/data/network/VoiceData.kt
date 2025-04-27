package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VoiceData(

        @SerializedName("wave_form")
        var waveForm: List<Int>?

): Serializable