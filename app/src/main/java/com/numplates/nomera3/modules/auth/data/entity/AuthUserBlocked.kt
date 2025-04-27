package com.numplates.nomera3.modules.auth.data.entity

import com.google.gson.annotations.SerializedName

data class AuthUserBlocked(
    @SerializedName("success")
    val success: SuccessUserBlockedAuth?
)

data class SuccessUserBlockedAuth(
    @SerializedName("error")
    val error: ErrorUserBlockedAuth?
)

data class ErrorUserBlockedAuth(
    @SerializedName("block_reason")
    val blockReason: String?,
    @SerializedName("block_time")
    val blockTime: Long?
)
