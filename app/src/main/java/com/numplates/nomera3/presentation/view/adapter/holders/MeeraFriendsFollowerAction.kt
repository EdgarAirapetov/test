package com.numplates.nomera3.presentation.view.adapter.holders

import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel

sealed class MeeraFriendsFollowerAction {
    class UserClick(val userId: Long): MeeraFriendsFollowerAction()
    class AddFriendsClick(val model: FriendsFollowersUiModel): MeeraFriendsFollowerAction()
    class AcceptRequestFriendClick(val model: FriendsFollowersUiModel): MeeraFriendsFollowerAction()
}
