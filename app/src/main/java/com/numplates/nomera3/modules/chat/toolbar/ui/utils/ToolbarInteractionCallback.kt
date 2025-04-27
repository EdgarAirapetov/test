package com.numplates.nomera3.modules.chat.toolbar.ui.utils

import android.view.View
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.chat.ChatRoomType
import com.numplates.nomera3.modules.chat.toolbar.ui.entity.UpdatedChatData
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager

interface ToolbarInteractionCallback {

    fun onClickMenuBackArrow()

    fun onClickDialogAvatar(
        userChat: UserChat?,
        hasMoments: Boolean = false,
        hasNewMoments: Boolean = false,
        view: View? = null
    )

    fun onClickGroupAvatar(roomId: Long?)

    fun onClickMenuGroupChatDetail(roomId: Long?)

    fun onClickMenuGroupChatMore(roomId: Long?)

    fun allowSwipeDirectionNavigator(direction: NavigatorViewPager.SwipeDirection?)

    fun setCallVariableSettings(isCallToggleVisible: Boolean, isMeAvailableForCalls: Int)

    fun startCallWithCompanion(companion: UserChat)

    fun showCallNotAllowedTooltip()

    fun setChatBackground(user: UserChat?, roomType: RoomType)

    fun showEnableNotificationsMessage()

    fun showDisableNotificationsMessage()

    fun errorWhenUpdatedNotification()

    fun updatedChatData(data: UpdatedChatData?, chatType: ChatRoomType)

    fun updateChatInputEnabled(isChatEnabled: Boolean)

    fun chatRequestStatus(isRoomChatRequest: Boolean)

    fun onBlockChatRequestClicked()

    fun allowSendMessageChatRequest()

    fun subscribeToUserClicked()

    fun dismissSubscriptionClicked()

    fun onClickDialogMoreItem()
}
