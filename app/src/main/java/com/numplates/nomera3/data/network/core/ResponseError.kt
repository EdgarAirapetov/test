package com.numplates.nomera3.data.network.core

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseError(
        @SerializedName("code") var code: Int,
        @SerializedName("message") var message: String?,
        @SerializedName("user_message") var userMessage: String?
) : Serializable