package com.numplates.nomera3.modules.chatfriendlist.presentation.paging

import com.meera.db.models.userprofile.UserSimple

interface FriendsDataCallback {
    fun getData(nameQuery: String?, startingFrom: Int, howMuch: Int): List<UserSimple>?
}
