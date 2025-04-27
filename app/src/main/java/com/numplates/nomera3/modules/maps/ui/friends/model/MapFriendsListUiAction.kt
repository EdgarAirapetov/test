package com.numplates.nomera3.modules.maps.ui.friends.model

import android.view.View


sealed interface MapFriendsListUiAction {

    data class ParticipantClicked(
        val itemUiModel: MapFriendListItem.MapFriendUiModel,
        val view: View? = null, val position: Int,
        val isAvatarClicked: Boolean = false,
    ) : MapFriendsListUiAction

    data class SendMessageClicked(val itemUiModel: MapFriendListItem.MapFriendUiModel, val position: Int) :
        MapFriendsListUiAction

    data class SearchFriends(val search: String) : MapFriendsListUiAction
    data class MapFriendListItemSelected(val item: MapFriendListItem.MapFriendUiModel?, val position: Int) :
        MapFriendsListUiAction

    object LoadNextPageRequested : MapFriendsListUiAction
    object UpdateSelectedUser : MapFriendsListUiAction
    object OpenFriendList : MapFriendsListUiAction
    object EnableFriendLayer : MapFriendsListUiAction
    object HideWidget : MapFriendsListUiAction
    object ShowWidget : MapFriendsListUiAction
    object OpenPeople : MapFriendsListUiAction
    object Close : MapFriendsListUiAction
}
