package com.numplates.nomera3.modules.auth.data.entity

import com.google.gson.annotations.SerializedName

data class AuthenticationError(
    @SerializedName("field")
    val field: String?,

    @SerializedName("reason")
    val reason: String?
)
