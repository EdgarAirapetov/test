package com.numplates.nomera3.modules.services.ui.entity

import android.view.View
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity

sealed class MeeraServicesUiAction {

    data class UserMomentClick(val userId: Long, val view: View) : MeeraServicesUiAction()

    data class UserClick(val userId: Long) : MeeraServicesUiAction()

    data object EventsClick : MeeraServicesUiAction()

    data object PeoplesClick : MeeraServicesUiAction()

    data object CommunitiesClick : MeeraServicesUiAction()

    data object SettingsClick : MeeraServicesUiAction()

    data object ClearRecentUsersClick : MeeraServicesUiAction()

    data object CancelClearingRecentUsersClick : MeeraServicesUiAction()

    data class RemoveRecommendedUserClick(val user: RecommendedPeopleUiEntity) : MeeraServicesUiAction()

    data class RecommendedUserClick(val user: RecommendedPeopleUiEntity) : MeeraServicesUiAction()

    data class AddRecommendedUserToFriendsClick(val user: RecommendedPeopleUiEntity) : MeeraServicesUiAction()

    data class RemoveRecommendedUserFromFriendsClick(val user: RecommendedPeopleUiEntity) : MeeraServicesUiAction()

    data class CommunityClick(val communityId: Int) : MeeraServicesUiAction()

    data class LoadNextRecommendedUsers(val offsetCount: Int, val rootAdapterPosition: Int) : MeeraServicesUiAction()

    data class LoadNextCommunities(val offset: Int, val rootAdapterPosition: Int) : MeeraServicesUiAction()

}
