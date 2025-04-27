package com.numplates.nomera3.modules.chatgroup.ui

import com.meera.db.models.chatmembers.ChatMember

interface MeeraChatMemberInfoCallback {
    fun onAvatarClicked(user: ChatMember?)

    fun onGroupUserDotsClicked(user: ChatMember)
}
