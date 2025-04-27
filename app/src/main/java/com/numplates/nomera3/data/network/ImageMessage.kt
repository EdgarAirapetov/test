package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ImageMessage(
        @SerializedName("data") var voiceData: ImageData,
        @SerializedName("link") var link: String
) : Serializable