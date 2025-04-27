package com.numplates.nomera3.modules.maps.ui.friends.model

import android.view.View
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel

sealed interface MapFriendsListUiEffect {

    data class OpenUserProfile(val userId: MapUserUiModel, val position: Int, val mapUserSnippetModel: UserSnippetModel) : MapFriendsListUiEffect
    data class OpenMoments(val userId: UserSnippetModel, val view: View?, val position: Int) : MapFriendsListUiEffect
    data class SendMessage(val userId: MapUserUiModel, val position: Int) : MapFriendsListUiEffect
    data class ListState(val isEmpty: Boolean) : MapFriendsListUiEffect
    data class MapFriendListItemSelected(val item: MapFriendListItem.MapFriendUiModel) : MapFriendsListUiEffect
    data object OpenFriends : MapFriendsListUiEffect
}
