package com.numplates.nomera3.modules.notifications.ui.basefragment

sealed class BaseNotificationFragmentEffect {
    data class OpenGroupChatFragment(val roomId: Long): BaseNotificationFragmentEffect()
}
