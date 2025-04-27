package com.numplates.nomera3.presentation.view.adapter.newchat

import android.view.View
import com.meera.db.models.chatmembers.ChatMember

interface IOnGroupUsersClicked {

    fun onGroupUserItemClicked(user: ChatMember?)

    fun onAvatarClicked(
        user: ChatMember?,
        view: View?,
        hasNewMoments: Boolean?
    )

    fun onGroupUserDotsClicked(user: ChatMember)

}
