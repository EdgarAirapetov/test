package com.numplates.nomera3.modules.chat.common.utils

import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData


fun resolveChatEntryData(
    companion: UserChat?,
    room: DialogEntity?,
    userId: String?,
    ownUserId: Long?,
    roomId: Long?,
    subscriptionDismissedUserIdList: List<Long> = emptyList(),
    messageByMeSent: Boolean
): ChatEntryData {
    val type = when(room?.type){
        ChatRoomType.DIALOG.key -> ChatRoomType.DIALOG
        ChatRoomType.GROUP.key -> ChatRoomType.GROUP
        else -> ChatRoomType.DIALOG
    }

    return ChatEntryData(
        roomType = type,
        userId = userId,
        ownUserId = ownUserId ?: 0L,
        roomId = roomId,
        companion = companion ?: room?.companion,
        room = room,
        wasSubscriptionDismissedEarlier = subscriptionDismissedUserIdList.contains(userId?.toLongOrNull()),
        messageByMeSent = messageByMeSent
    )
}
