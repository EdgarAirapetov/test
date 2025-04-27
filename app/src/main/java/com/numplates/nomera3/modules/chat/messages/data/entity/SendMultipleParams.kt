package com.numplates.nomera3.modules.chat.messages.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel

data class SendMultipleParams(

    @SerializedName("user_ids")
    val userIds: List<Long>?,

    @SerializedName("room_ids")
    val roomIds: List<Long>?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("attachment")
    val attachment: ChatAttachmentPartialUploadModel?,

    @SerializedName("attachments")
    val attachments: List<ChatAttachmentPartialUploadModel>?

)
