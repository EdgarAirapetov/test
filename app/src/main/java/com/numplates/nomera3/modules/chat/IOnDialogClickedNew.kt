package com.numplates.nomera3.modules.chat

import com.meera.db.models.dialog.DialogEntity

interface IOnDialogClickedNew {

    fun onRoomClicked(dialog: DialogEntity?)

    fun onRoomLongClicked(dialog: DialogEntity?)

    fun onAvatarClicked(dialog: DialogEntity?)

}
