package com.numplates.nomera3.modules.chat.toolbar.domain.mapper

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.core.extensions.pluralString
import com.meera.core.utils.timeAgoChatToolbarStatus
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.toolbar.data.entity.OnlineChatStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatOnlineStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.NetworkChatStatus


class ChatOnlineStatusMapper(private val context: Context) {

    fun map(entryData: ChatEntryData, dbEntity: OnlineChatStatusEntity?): ChatOnlineStatusEntity {
        return when (entryData.roomType) {
            ChatRoomType.DIALOG -> dialogChatStatusHandler(
                status = dbEntity?.status ?: NetworkChatStatus.OFFLINE.key,
                timestamp = dbEntity?.timestamp
            )
            ChatRoomType.GROUP -> groupChatStatusHandler(
                membersCount = entryData.room?.membersCount ?: 0,
                onlineCount = dbEntity?.count ?: 0
            )
        }
    }

    private fun dialogChatStatusHandler(status: String?, timestamp: Int?): ChatOnlineStatusEntity {
        return when (status) {
            NetworkChatStatus.ONLINE.key -> {
                ChatOnlineStatusEntity(
                    isShowStatus = true,
                    networkStatus = NetworkChatStatus.ONLINE,
                    isShowDotIndicator = true,
                    message = context.getString(R.string.online_status_txt)
                )
            }
            NetworkChatStatus.OFFLINE.key -> {
                ChatOnlineStatusEntity(
                    isShowStatus = timestamp != null && timestamp > 0,
                    networkStatus = NetworkChatStatus.OFFLINE,
                    isShowDotIndicator = true,
                    message = if (timestamp != null) {
                        timeAgoChatToolbarStatus(context, timestamp.toLong() * 1000L)
                    } else String.empty()
                )
            }
            else -> {
                ChatOnlineStatusEntity(
                    isShowStatus = false,
                    networkStatus = NetworkChatStatus.OFFLINE,
                    isShowDotIndicator = true,
                    message = String.empty()
                )
            }
        }
    }

    private fun groupChatStatusHandler(
        membersCount: Int,
        onlineCount: Int
    ): ChatOnlineStatusEntity {
        var message = context.pluralString(R.plurals.group_members_plural, membersCount)
        if (onlineCount > 0) {
            message += context.getString(R.string.group_chat_ststus_members_online, onlineCount)
        }
        return ChatOnlineStatusEntity(
            isShowStatus = true,
            networkStatus = NetworkChatStatus.ONLINE,
            isShowDotIndicator = false,
            message = message
        )
    }

}
