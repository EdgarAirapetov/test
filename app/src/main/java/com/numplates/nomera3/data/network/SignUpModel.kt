package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SignUpModel(
        @SerializedName("token") var token: String?,
        @SerializedName("user_message") var userMessage: String?
): Serializable