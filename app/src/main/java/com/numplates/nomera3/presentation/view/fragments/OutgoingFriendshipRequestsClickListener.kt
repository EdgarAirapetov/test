package com.numplates.nomera3.presentation.view.fragments

import com.meera.db.models.userprofile.UserSimple

interface OutgoingFriendshipRequestsClickListener {

    fun onItemClicked(userSimple: UserSimple)

    fun onActionClicked(userSimple: UserSimple)

    fun onLoadMore() {

    }
}
