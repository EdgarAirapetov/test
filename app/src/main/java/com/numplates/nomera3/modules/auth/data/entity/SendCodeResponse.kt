package com.numplates.nomera3.modules.auth.data.entity

import com.google.gson.annotations.SerializedName

data class SendCodeResponse(
    @SerializedName("timeout") val timeout: Long? = null,

    @SerializedName("block") val block: SendCodeBlock? = null,

    @SerializedName("success") val success: SuccessUserBlockedAuth? = null
)

data class SendCodeBlock(
    @SerializedName("type") val type: String? = null,
    @SerializedName("time") val time: Long? = null
) {
    companion object {
        const val TYPE_COUNTRY = "country"
    }
}
