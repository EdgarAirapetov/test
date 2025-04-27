package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class FriendsListViewEvents {
    object OnClearSearchAdapter : FriendsListViewEvents()
    object OnErrorAddFriend : FriendsListViewEvents()
    object OnErrorRemoveFriend : FriendsListViewEvents()
    object OnFriendRejected: FriendsListViewEvents()
    object OnErrorAction: FriendsListViewEvents()

    object NoFriendsEvent: FriendsListViewEvents()
    object HasFriendsEvent: FriendsListViewEvents()

    object NoIncomigRequests: FriendsListViewEvents()
    object HasIncomingRequests: FriendsListViewEvents()
}