package com.numplates.nomera3.modules.chat.messages.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.MessageEntity

data class MessagesDto(
    @SerializedName("messages")
    val messages: List<MessageEntity>,

    @SerializedName("rest")
    val rest: Int,

    @SerializedName("unreaded")
    val unread: Int
)
