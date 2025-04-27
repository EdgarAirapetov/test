package com.numplates.nomera3.modules.chat.data

import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.meera.db.models.dialog.DialogEntity

data class ChatEntryData(
        val roomType: ChatRoomType,
        val userId: String? = null,
        val ownUserId: Long,
        val roomId: Long? = null,
        val companion: UserChat? = null,
        val room: DialogEntity? = null,
        val wasSubscriptionDismissedEarlier: Boolean = false,
        val messageByMeSent: Boolean = false
)
