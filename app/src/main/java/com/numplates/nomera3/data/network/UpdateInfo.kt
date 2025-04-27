package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdateInfo(
        @SerializedName("url") var url: String,
        @SerializedName("text") var text: String
): Serializable