package com.numplates.nomera3.modules.peoples.ui.content.action

import android.view.View
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity

sealed class FriendsContentActions {
    object FindFriendsContentUiActions : FriendsContentActions()
    object OnReferralClicked : FriendsContentActions()
    object ShowOnboardingAction : FriendsContentActions()
    object OnRefreshContentByTabBarAction : FriendsContentActions()
    object OnRefreshContentBySwipe : FriendsContentActions()
    object GetNextTopUsersAction : FriendsContentActions()
    object SetCommunityTooltipShownAction : FriendsContentActions()
    object OnShowBumpClicked : FriendsContentActions()
    object LogCommunitySectionUiAction : FriendsContentActions()
    object OnSyncContactsUiAction : FriendsContentActions()
    object LogInviteFriendAction : FriendsContentActions()
    data class OnDialogSyncContactsPositiveButtonClickedUiAction(
        val showSyncContactsWelcome: Boolean
    ) : FriendsContentActions()

    object ReadContactsPermissionGrantedUiAction : FriendsContentActions()
    data class OnBloggerSubscribeClicked(val user: PeopleInfoUiEntity) : FriendsContentActions()
    data class OnBloggerUnSubscribeClicked(val user: PeopleInfoUiEntity) : FriendsContentActions()
    data class OnUserClicked(
        val entity: PeopleInfoUiEntity,
    ) : FriendsContentActions()

    data class OnUserAvatarClicked(
        val entity: PeopleInfoUiEntity,
        val view: View?
    ) : FriendsContentActions()

    data class OnRelatedUserClicked(
        val entity: RecommendedPeopleUiEntity
    ) : FriendsContentActions()

    data class OnVideoPostClicked(
        val entity: BloggerMediaContentUiEntity.BloggerVideoContentUiEntity
    ) : FriendsContentActions()

    data class OnImagePostClicked(
        val entity: BloggerMediaContentUiEntity.BloggerImageContentUiEntity
    ) : FriendsContentActions()

    data class OnMediaPlaceholderClicked(
        val userId: Long,
        val postId: Long,
        val user: PeopleInfoUiEntity
    ) : FriendsContentActions()

    data class OnRecommendedUserAddToFriendClicked(
        val entity: RecommendedPeopleUiEntity
    ) : FriendsContentActions()

    data class OnRecommendedUserRemoveFromFriendsClicked(
        val userId: Long
    ) : FriendsContentActions()

    data class GetNextRelatedUsers(
        val offsetCount: Int,
        val rootAdapterPosition: Int
    ) : FriendsContentActions()

    data class InitPeopleWelcomeUiAction(
        val needToShowWelcome: Boolean,
        val isCalledFromBottomNav: Boolean
    ) : FriendsContentActions()

    data class ContactsPermissionDeniedUiAction(
        val deniedAndNoRationaleNeededAfterRequest: Boolean
    ) : FriendsContentActions()

    data class OnHideRelatedUserUiAction(val userId: Long) : FriendsContentActions()

    data class OnSuccessSyncContactsClosedUiAction(val syncCount: Int) : FriendsContentActions()

    data class OnSuccessSyncContactsClosedButtonUiAction(val syncCount: Int) : FriendsContentActions()

    data class LogSyncContactsGoToSettings(
        val showSyncContactsWelcome: Boolean
    ) : FriendsContentActions()

    data class LogSyncContactsGoToSettingsClosedUiAction(
        val showSyncContactsWelcome: Boolean
    ) : FriendsContentActions()

    data class LogSyncContactsDialogClosedUiAction(
        val showSyncContactsWelcome: Boolean
    ) : FriendsContentActions()

    object ClearRecentUsersUiAction : FriendsContentActions()

    data class SelectRecentItemUiAction(
        val recentUser: RecentUserUiModel
    ) : FriendsContentActions()

    data class SelectUserSearchResultUiAction(
        val user: UserSearchResultUiEntity
    ) : FriendsContentActions()

    data class AddUserSearchResultUiAction(
        val user: UserSearchResultUiEntity
    ) : FriendsContentActions()

    data class OpenUserMomentsAction(
        val fromView: View,
        val user: UserSearchResultUiEntity
    ) : FriendsContentActions()

    data class CheckIfNeedToScrollToUserFromPush(
        val userId: Long?,
        val currentList: List<PeoplesContentUiEntity>?
    ) : FriendsContentActions()

    data class KeyboardVisibilityChanged(
        val isKeyboardOpened: Boolean
    ) : FriendsContentActions()
}
