package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.presentation.view.utils.apphints.Hint

sealed class ChatRoomsViewEvent {
    class OnShowCreateGroupChatAppHint(val hint: Hint?) : ChatRoomsViewEvent()
    class OnNavigateToChatEvent(val roomData: DialogEntity) : ChatRoomsViewEvent()
    data object ShowSwipeToShowChatSearchEvent: ChatRoomsViewEvent()
}
