package com.numplates.nomera3.presentation.view.adapter

import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel

interface SubscriberFriendActionCallback {
    fun onUserClicked(model: FriendsFollowersUiModel)
    fun onUserActionIconClicked(model: FriendsFollowersUiModel)
}