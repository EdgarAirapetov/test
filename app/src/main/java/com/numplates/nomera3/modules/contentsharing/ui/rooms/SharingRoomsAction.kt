package com.numplates.nomera3.modules.contentsharing.ui.rooms

import com.meera.core.base.viewmodel.Action
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem

sealed class SharingRoomsAction : Action {

    data object LoadAvailableChatsToShare : SharingRoomsAction()

    data object SendContentToChats : SharingRoomsAction()

    class ShareContentToChats(val content: String?) : SharingRoomsAction()

    class QueryShareItems(val query: String?) : SharingRoomsAction()

    class ChangeSelectedState(val item: UIShareItem, val isChecked: Boolean) : SharingRoomsAction()
}
