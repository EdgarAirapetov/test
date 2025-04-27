package com.numplates.nomera3.modules.chat.notification

import com.meera.db.models.dialog.UserChat

data class ChatPushData(
    val roomId: Long,
    val message: String,
    val isGroupChat: Boolean,
    val chatName: String,
    val user: UserChat? = null,
    var eventId: String? = null,
    val image: String? = null,
    val showReply: Boolean? = null,
    val isResended: Boolean? = null
)
