package com.numplates.nomera3.modules.user.ui.event

import androidx.work.WorkInfo
import com.meera.core.base.viewmodel.Effect
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsPreviewModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment

sealed class UserInfoViewEffect : Effect {
    data object ShowPhotosCounterTooltip : UserInfoViewEffect()
    data object ShowBubbleStarsTooltip : UserInfoViewEffect()
    class ShowBirthdayDialog(val isBirthdayToday: Boolean) : UserInfoViewEffect()
    class CommunityChanges(val communityListEvents: CommunityListEvents) : UserInfoViewEffect()
    class AvatarReadyToUpload(val path: String, val avatarState: String? = null) : UserInfoViewEffect()
    class OnAnimatedAvatarSaved(val path: String) : UserInfoViewEffect()
    class OnFailureChangeAvatar(var imagePath: String? = null) : UserInfoViewEffect()
    class OnGoneProgressUserAvatar(var imagePath: String? = null, var createAvatarPost: Int) : UserInfoViewEffect()
    class OnSuccessRequestAddFriend(
        var message: String? = null,
        val messageRes: Int? = null
    ) : UserInfoViewEffect()

    object OnFailureRequestAddFriend : UserInfoViewEffect()
    class OnSuccessCancelFriendRequest(
        val unsubscribed: Boolean
    ) : UserInfoViewEffect()

    class OnSuccessRemoveFriend(
        val unsubsribed: Boolean,
        val message: String = ""
    ) : UserInfoViewEffect()

    object GoToMarket : UserInfoViewEffect()
    object OpenAddPhoto : UserInfoViewEffect()
    class AddSuggestionFriends(
        val friendStatus: Int = -1,
        val approved: Boolean = false,
        val influenecer: Boolean = false
    ) : UserInfoViewEffect()

    object OnFailureRemoveFriend : UserInfoViewEffect()
    class OnSuccessBlockUser(val isBlock: Boolean? = null) : UserInfoViewEffect()
    object OnSuccessDisableChat : UserInfoViewEffect()
    object OnSuccessEnableChat : UserInfoViewEffect()
    object OnSuccessEnableChatCompanionBlocked : UserInfoViewEffect()
    object OnFailureBlockUser : UserInfoViewEffect()
    object OnFailureEnableChat : UserInfoViewEffect()
    object OnFailureDisableChat : UserInfoViewEffect()
    object OnHideProgressUploadImage : UserInfoViewEffect()
    object OnUnsubscribed : UserInfoViewEffect()
    object ShowProfileStatistics : UserInfoViewEffect()
    class OnSubscribed(val message: String = "") : UserInfoViewEffect()

    /**
     * Send subscribe failure error to view with a message.
     * Use default message from the app if [message] is null.
     */
    class OnSubscribeFailure(val message: String? = null) : UserInfoViewEffect()

    /**
     * Send unsubscribe failure error to view with a message.
     * Use default message from the app if [message] is null.
     */
    class OnUnsubscribeFailure(val message: String? = null) : UserInfoViewEffect()

    class OnCreateAvatarPostSettings(
        val privacySettingModel: PrivacySettingModel?,
        val imagePath: String,
        val animation: String?
    ) : UserInfoViewEffect()


    object OnSuccessEnableSubscriptionNotification : UserInfoViewEffect()
    object OnSuccessDisableSubscriptionNotification : UserInfoViewEffect()
    object OnFailChangeSubscriptionNotification : UserInfoViewEffect()
    object OnRefreshUserRoad : UserInfoViewEffect()

    class ShowShareProfileDialog(
        val profileLink: String,
        val profile: UserProfileUIModel
    ) : UserInfoViewEffect()


    object AllowSwipeAndGoBack : UserInfoViewEffect()
    object OpenCameraToChangeAvatar : UserInfoViewEffect()
    object OpenAvatarCreator : UserInfoViewEffect()


    class ShowSuccessCopyProfile(val isProfileDeleted: Boolean) : UserInfoViewEffect()
    class SaveImageWithPermission(val image: String) : UserInfoViewEffect()
    class CallToUser(val iCanCall: Boolean) : UserInfoViewEffect()
    class UploadImageInfo(val nonNullWork: WorkInfo) : UserInfoViewEffect()
    class ShowToastEvent(
        val message: String? = null,
        val isSuccess: Boolean,
        val messageRes: Int? = null
    ) : UserInfoViewEffect()

    class SetSnippetState(val snippetState: SnippetState.StableSnippetState) : UserInfoViewEffect()
    class AuthAndOpenFriendsList(
        val userId: Long,
        val actionType: MeeraFriendsHostFragment.SelectedPage? = null,
        val name: String
    ) : UserInfoViewEffect()

    class OnMomentsPreviewUpdated(
        val previews: List<UserMomentsPreviewModel>,
        val hasNewMoments: Boolean
    ) : UserInfoViewEffect()

    class SubmitAvatars(val items: List<PhotoModel>, val count: Int?, val currentPosition: Int?) : UserInfoViewEffect()
}

sealed class UserProfileTooltipEffect : UserInfoViewEffect() {
    object ShowUniqueNameTooltip : UserProfileTooltipEffect()
    object ShowAvatarCreateTooltip : UserProfileTooltipEffect()
    class ShowReferralTooltip(val position: Int) : UserProfileTooltipEffect()
    object ShowUserTopMarkerTooltip : UserProfileTooltipEffect()
    object ShowUserSubscribersTooltip : UserProfileTooltipEffect()
    object ShowUniqueNameTooltipCopied : UserProfileTooltipEffect()
}

sealed class PhoneCallsViewEffect : UserInfoViewEffect() {
    object OnSuccessDisableCalls : PhoneCallsViewEffect()
    object OnSuccessEnableCalls : PhoneCallsViewEffect()
    object OnFailureEnableCalls : PhoneCallsViewEffect()
    object OnFailureDisableCalls : PhoneCallsViewEffect()
}

sealed class RoadPostViewEffect : UserInfoViewEffect() {
    object OnSuccessHidePosts : RoadPostViewEffect()
    object OnFailureHidePosts : RoadPostViewEffect()
    object OnSuccessUnhidePosts : RoadPostViewEffect()
    object OnFailureUnhidePosts : RoadPostViewEffect()
}

sealed class UserProfileDialogNavigation : UserInfoViewEffect() {
    class ShowConfirmDialogRemoveFromFriend(
        val isSubscribed: Boolean,
        val friendName: String
    ) : UserProfileDialogNavigation()

    class ShowCancelFriendshipRequestDialog(val isSubscribed: Boolean) : UserProfileDialogNavigation()

    class ShowFriendIncomingStatusMenu(
        val isSubscribed: Boolean,
        val approved: Boolean,
        val influencer: Boolean,
        val friendStatus: Int,
        val isNotificationsEnabled: Boolean = false,
        val isNotificationsAvailable: Boolean = false
    ) : UserProfileDialogNavigation()

    class ShowFriendIncomingSubscribeStatusMenuMeera(
        val approved: Boolean,
        val influencer: Boolean,
        val friendStatus: Int,
        val isNotificationsEnabled: Boolean = false,
        val isNotificationsAvailable: Boolean = false
    ) : UserProfileDialogNavigation()

    class ShowFriendIncomingUnsubscribeStatusMenuMeera(
        val approved: Boolean,
        val influencer: Boolean,
        val friendStatus: Int,
        val isNotificationsEnabled: Boolean = false,
        val isNotificationsAvailable: Boolean = false
    ) : UserProfileDialogNavigation()

    class ShowUnsubscribeMenu(
        val isProfileSuggestionFloorShown: Boolean,
        val isNotificationsAvailable: Boolean,
        val isNotificationsEnabled: Boolean,
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowNoFriendSubscribeMenu(
        val isNotificationsAvailable: Boolean,
        val isNotificationsEnabled: Boolean,
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowUnsubscribeMenuMeera(
        val isNotificationsAvailable: Boolean,
        val isNotificationsEnabled: Boolean,
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowSubscribeMenuMeera(
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowFriendUnsubscribeMenu(
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowFriendSubscribeMenu(
        val isNotificationsAvailable: Boolean,
        val isNotificationsEnabled: Boolean,
        val friendStatus: Int = -1,
    ) : UserProfileDialogNavigation()

    class ShowSuggestion(val isSuggestionShow: Boolean) : UserProfileDialogNavigation()

    class SubscribeRequestAction(
        val isSubscribed: Boolean,
        val userId: Long,
        val friendStatus: Int,
        val approved: Boolean,
        val topContent: Boolean,
        val message: String
    ) : UserProfileDialogNavigation()

    class ShowDotsMenu(
        val profileLink: String,
        val profile: UserProfileUIModel,
        val isMe: Boolean
    ) : UserProfileDialogNavigation()

    class ShowScreenshotPopup(
        val userLink: String
    ) : UserProfileDialogNavigation()
}

sealed class UserProfileNavigation : UserInfoViewEffect() {
    class OpenPeopleFragment(
        val showSyncContactsWelcome: Boolean = false
    ) : UserProfileNavigation()
    object OpenProfileEdit : UserProfileNavigation()
    object OpenAddVehicle : UserProfileNavigation()
    class OpenComplainFragment(
        val userId: Long,
        val where: AmplitudePropertyWhere
    ) : UserProfileNavigation()

    object OpenCommunityEditCreate : UserProfileNavigation()
    class OpenUserGiftsListFragment(
        val user: UserProfileUIModel,
        val where: AmplitudePropertyWhere,
        val scrollToBottom: Boolean
    ) : UserProfileNavigation()

    class OpenUserGiftsFragment(
        val user: UserProfileUIModel,
        val where: AmplitudePropertyWhere
    ) : UserProfileNavigation()

    class OpenProfile(val userId: Long) : UserProfileNavigation()
    data class NavigateToPeopleFragment(
        val showSwitcher: Boolean,
        val showSyncContactsWelcome: Boolean
    ) : UserProfileNavigation()

    class NavigateToPostFragment(
        val showMediaGallery: Boolean
    ) : UserProfileNavigation()

    class ShowMap(val mapUser: MapUserUiModel) : UserProfileNavigation()
    class ShowGiftScreen(
        val gift: GiftItemUiModel,
        val userId: Long,
        val userName: String?,
        val goBackTwice: Boolean,
        val biAmplitudeWhere: AmplitudePropertyWhere,
        val birth: Long?
    ) : UserProfileNavigation()

    class StartDialog(
        val userId: Long,
        val where: AmplitudePropertyWhere,
        val fromWhere: AmplitudePropertyChatCreatedFromWhere
    ) : UserProfileNavigation()

    class OpenVehicleList(
        val userId: Long,
        val accountType: Int?,
        val accountColor: Int?
    ) : UserProfileNavigation()

    class OpenVehicle(
        val userId: Long,
        val vehicleId: String
    ) : UserProfileNavigation()

    class OpenMapSettingsFragment(
        val settingValue: Int?,
        val countBlacklist: Int?,
        val countWhitelist: Int?
    ) : UserProfileNavigation()

    class OpenCommunityFeed(
        val communityId: Int
    ) : UserProfileNavigation()

    object OpenSubscriptions : UserProfileNavigation()
    object OpenSubscribers : UserProfileNavigation()
    object CloseFloorCongratulation : UserProfileNavigation()
    class OpenGridProfile(val userId: Long, val photoCount: Int) : UserProfileNavigation()
    class OpenProfilePhotoViewer(
        val isMe: Boolean,
        val position: Int,
        val userId: Long,
        val isAvatarPhoto: Boolean = false
    ) : UserProfileNavigation()

    class GoToGroupTab(val where: AmplitudePeopleWhereProperty) : UserProfileNavigation()
    object GoToAllGroupTab : UserProfileNavigation()

    object ShowInternetError : UserProfileNavigation()

    class OpenMoments(
        val openedFrom: MomentClickOrigin,
        val existUserId: Long,
        val hasNewMoments: Boolean?
    ) : UserProfileNavigation()
}
