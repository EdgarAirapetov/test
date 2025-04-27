package com.numplates.nomera3.modules.search.ui.entity.event

import android.view.View
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

sealed class UserSearchViewEvent : SearchBaseViewEvent() {
    data class AddUser(val user: SearchItem.User) : UserSearchViewEvent()
    data class SelectUser(
        val userId: Long,
        val isRecent: Boolean,
        val approved: Boolean,
        val topContentMaker: Boolean
    ) : UserSearchViewEvent()
    data class OpenUserMoments(
        val userId: Long,
        val view: View?,
        val hasNewMoments: Boolean
    ) : UserSearchViewEvent()
}
