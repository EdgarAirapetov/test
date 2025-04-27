package com.numplates.nomera3.modules.share.ui.entity

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_SEND

data class UIShareMessageEntity (
    val messageId: String,
    val roomId: Long,
    val message: String?,
    val images: List<String>,
    val video: String?,
    val isAudio: Boolean,
    val isGiphy: Boolean,
)

fun MessageEntity.toUIShareMessage(): UIShareMessageEntity {
    val images = mutableListOf<String>()
    val video = attachment.makeMetaMessageWithVideo()?.preview
    val isAudio = attachment.type == TYPING_TYPE_AUDIO
    val text = if (itemType == ITEM_TYPE_SHARE_PROFILE_SEND
        || itemType ==  ITEM_TYPE_SHARE_PROFILE_RECEIVE) "" else content

    if (attachment.type == TYPING_TYPE_GIF) images.add(attachment.url)
    attachments.forEach { images.add(it.url) }
    return UIShareMessageEntity(
        messageId = msgId,
        roomId = roomId,
        message = text,
        images = images,
        video = video,
        isAudio = isAudio,
        isGiphy = isShowGiphyWatermark?: false
    )
}
