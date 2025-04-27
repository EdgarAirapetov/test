package com.numplates.nomera3.modules.chat.messages.data.entity

import com.google.gson.annotations.SerializedName

data class SendMessageDto(
    @SerializedName("id")
    val messageId: String,
    @SerializedName("room_id")
    val roomId: Long
)
