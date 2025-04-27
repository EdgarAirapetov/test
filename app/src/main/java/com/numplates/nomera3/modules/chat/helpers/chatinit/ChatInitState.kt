package com.numplates.nomera3.modules.chat.helpers.chatinit

import com.meera.db.models.dialog.DialogEntity

sealed interface ChatInitState {

    class OnTransitedByListRooms(val room: DialogEntity?) : ChatInitState

    class OnTransitedByProfile(val room: DialogEntity) : ChatInitState

    class OnUpdateRoomData(val room: DialogEntity?): ChatInitState

}
