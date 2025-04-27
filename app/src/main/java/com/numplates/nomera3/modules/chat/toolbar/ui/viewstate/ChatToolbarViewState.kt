package com.numplates.nomera3.modules.chat.toolbar.ui.viewstate

import com.numplates.nomera3.modules.chat.data.ChatEntryData
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ChatOnlineStatusEntity
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.ToolbarEntity

sealed class ChatToolbarViewState {

    class OnUpdateData(
        val entryData: ChatEntryData,
        val toolbar: ToolbarEntity
    ) : ChatToolbarViewState() {
        override fun toString(): String {
            return "STATE OnUpdateData(toolbar=$toolbar)"
        }
    }

    class OnUpdateAvatarMomentsState(
        val hasMoments: Boolean,
        val hasNewMoments: Boolean
    ) : ChatToolbarViewState()

    class OnUpdateOnlineStatus(val status: ChatOnlineStatusEntity) : ChatToolbarViewState() {
        override fun toString(): String {
            return "STATE OnlineStatus:$status"
        }
    }

    class OnUpdateTyping(val typingText: String) : ChatToolbarViewState()

    data object OnHideStatus : ChatToolbarViewState()

    class OnUpdateNotificationStatus(
        val entryData: ChatEntryData,
        val isSetNotifications: Boolean
    ) : ChatToolbarViewState()

    data object OnErrorUpdateNotificationStatus : ChatToolbarViewState()

    class OnUpdateChatInputState(val chatEnabled: Boolean) : ChatToolbarViewState()
}
