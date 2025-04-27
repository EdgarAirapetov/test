package com.numplates.nomera3.modules.chat.toolbar.ui.utils

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.core.utils.timeAgoChatToolbarStatus
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatOnlineStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.NetworkChatStatus
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ToolbarEntity
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper

class ToolbarEntityMapper(private val context: Context) {

    fun map(entryData: ChatEntryData): ToolbarEntity {
        val moments = entryData.companion?.moments ?: UserMomentsDto.emptyMoments()
        return when(entryData.roomType) {
            ChatRoomType.DIALOG -> {
                ToolbarEntity(
                    title = entryData.companion?.name ?: String.empty(),
                    avatar = entryData.companion?.avatarSmall ?: String.empty(),
                    onlineStatus = mapUserOnlineStatus(entryData.companion),
                    accountType = entryData.companion?.accountType ?: 0,
                    approved = entryData.companion?.approved == 1,
                    topContentMaker = entryData.companion?.topContentMaker == 1,
                    moments = UserMomentsMapper.mapUserMomentsModel(moments)
                )
            }
            ChatRoomType.GROUP -> {
                ToolbarEntity(
                    title = entryData.room?.title ?: String.empty(),
                    avatar = entryData.room?.groupAvatar ?: String.empty()
                )
            }
        }
    }

    private fun mapUserOnlineStatus(user: UserChat?): ChatOnlineStatusEntity? {
        if (user == null) return null
        val onlineStatus = user.onlineStatus
        val isOnline = onlineStatus?.online == true
        val ts = onlineStatus?.lastActive
        val message = if (ts != null) timeAgoChatToolbarStatus(context, ts.toLong() * 1000L) else String.empty()
        return ChatOnlineStatusEntity(
            isShowStatus = true,
            networkStatus = if (isOnline) NetworkChatStatus.ONLINE else NetworkChatStatus.OFFLINE,
            isShowDotIndicator = true,
            message = if (isOnline) context.getString(R.string.online_status_txt) else message
        )
    }

}
