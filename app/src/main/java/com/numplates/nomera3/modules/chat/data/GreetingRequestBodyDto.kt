package com.numplates.nomera3.modules.chat.data

import com.google.gson.annotations.SerializedName

data class GreetingRequestBodyDto(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("sticker_id")
    val stickerId: Int?
)
