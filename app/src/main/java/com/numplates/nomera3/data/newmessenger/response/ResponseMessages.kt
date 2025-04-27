package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.MessageEntity


/**
 * Json Messages response from server
 */
data class ResponseMessages(
    @SerializedName("response")
    val response: Messages,
    @SerializedName("status")
    val status: String
)

data class Messages(
    @SerializedName("messages")
    val messages: List<MessageEntity>,
    @SerializedName("unreaded")
    val unread: Int,
    @SerializedName("rest")
    val rest: Int,
    @SerializedName("error")
    val error: String?,
)
