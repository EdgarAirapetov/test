package com.numplates.nomera3.modules.chat.messages.data

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.MessageAttachment

sealed interface EditMessageDtos {

    data class Request(
        @SerializedName("id") val messageId: String,
        @SerializedName("room_id") val roomId: Long,
        @SerializedName("new_message") val newMessage: NewMessage
    )

    data class Response(
        @SerializedName("id") val messageId: String,
        @SerializedName("room_id") val roomId: Long
    )

    /**
     * Message's content to edit. Please note that for 1 and 1+ images different params are used.
     *
     * @param content updated message text
     * @param attachment [MessageAttachment] if there is an attachment or empty string to clear field.
     * @param attachments list of [MessageAttachment] if there are more that 1 image or an empty list.
     */
    data class NewMessage(
        @SerializedName("content") val content: String? = null,
        @SerializedName("attachment") val attachment: Any? = null,
        @SerializedName("attachments") val attachments: List<MessageAttachment>? = null,

        //TODO attachments
        // https://nomera.atlassian.net/browse/BR-18091,
        // https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/1551007779/Messages#Edit)
    )
}
