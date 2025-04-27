package com.numplates.nomera3.presentation.view.adapter.newfriends

import com.meera.db.models.userprofile.UserSimple

sealed class FriendsListAction {
    class DeleteUserClick(val model: FriendModel): FriendsListAction()
    class OpenProfileClick(val userId: Long): FriendsListAction()
    class RejectFriendClick(val model: FriendModel): FriendsListAction()
    class ConfirmFriendClick(val model: FriendModel): FriendsListAction()
    class CancelOutgoingFriendshipClick(val model: UserSimple): FriendsListAction()
}
