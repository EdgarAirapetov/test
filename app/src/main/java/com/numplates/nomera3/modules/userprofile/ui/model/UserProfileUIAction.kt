package com.numplates.nomera3.modules.userprofile.ui.model

import android.net.Uri
import com.meera.core.base.viewmodel.Action
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsCreateTapWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendRelationshipProperty
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.GroupUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel

sealed interface UserProfileUIAction : Action {
    data class AvatarsAlreadySet(val photosCount: Int) : UserProfileUIAction
    object UpdateButtonClicked : UserProfileUIAction
    object AddPhoto : UserProfileUIAction
    object ShowDotsMenuAction : UserProfileUIAction
    object OnShareProfileClickAction : UserProfileUIAction
    object OnCopyProfileClickedAction : UserProfileUIAction
    object OnChatPrivacyClickedAction : UserProfileUIAction
    object OnCallPrivacyClickedAction : UserProfileUIAction
    object OnBlacklistUserClickedAction : UserProfileUIAction
    object UnsubscribeFromUserClickedAction : UserProfileUIAction
    data class SaveUserAvatarToGalleryAction(val position: Int) : UserProfileUIAction
    object OnOpenProfileClicked : UserProfileUIAction
    object OnAvatarChangeClicked : UserProfileUIAction
    object CreateAvatar : UserProfileUIAction
    data object OnEditorOpen : UserProfileUIAction
    class EditAvatar(val nmrAmplitude: NMRPhotoAmplitude?) : UserProfileUIAction
    object StartChatClick : UserProfileUIAction
    object OnGiftClick : UserProfileUIAction
    object OnGiftsListClick : UserProfileUIAction
    object OnCongratulationClick : UserProfileUIAction
    object OnCloseCongratulationClick : UserProfileUIAction
    object OnFragmentStart : UserProfileUIAction
    object TopMarkerClick : UserProfileUIAction
    object SubscribersCountClick : UserProfileUIAction
    object UniqueNameClick : UserProfileUIAction
    object OnAllVehiclesClick : UserProfileUIAction
    object OnAddVehicleClick : UserProfileUIAction
    object OnPrivacyClick : UserProfileUIAction
    object RequestMapPrivacySettings : UserProfileUIAction
    object OnFindGroup : UserProfileUIAction
    object OnAllGroupClick : UserProfileUIAction
    object OnCreateGroupClick : UserProfileUIAction
    object OnFriendsListClicked : UserProfileUIAction
    object OnSubscribersListClicked : UserProfileUIAction
    object OnSubscriptionsListClicked : UserProfileUIAction
    object DisabledSubscriberFloorClicked : UserProfileUIAction
    object OnMutualFriendsClicked : UserProfileUIAction
    object LoadMoreAvatars : UserProfileUIAction

    class OnGroupClicked(val group: GroupUIModel) : UserProfileUIAction
    class OnVehicleClick(val vehicle: VehicleUIModel) : UserProfileUIAction
    class OnComplainClick(val userId: Long, val where: AmplitudePropertyWhere) : UserProfileUIAction
    class FragmentViewCreated(val isUserSnippet: Boolean) : UserProfileUIAction
    class OnHolderBind(val position: Int) : UserProfileUIAction
    class ChangePostsPrivacyClickedAction(val needToHidePost: Boolean) : UserProfileUIAction
    object OnGridGalleryClicked : UserProfileUIAction

    class RemoveFriendClickedAction(
        val cancellingFriendRequest: Boolean,
        val message: String = ""
    ) : UserProfileUIAction

    class OnLiveAvatarChanged(val avatarJson: String) : UserProfileUIAction
    class OnPublishOptionsSelected(
        val imagePath: String,
        val animation: String?,
        val createAvatarPost: Int,
        val saveSettings: Int,
        val amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType
    ) : UserProfileUIAction

    class OnAddFriendClicked(
        val friendStatus: Int,
        val approved: Boolean,
        val influencer: Boolean
    ) : UserProfileUIAction

    class OnMapClicked(
        val lat: Double?,
        val lng: Double?
    ) : UserProfileUIAction

    class OnSendGiftClicked(val gift: GiftItemDto) : UserProfileUIAction
    class UploadToGallery(val images: List<Uri>) : UserProfileUIAction
    class ClickSubscribeNotification(val isEnabled: Boolean) : UserProfileUIAction
    class RemoveFriendAndUnsubscribe(val cancellingFriendRequest: Boolean) : UserProfileUIAction
    class UnsubscribeFromUserDialogClickedAction(
        val isApproved: Boolean,
        val topContentMaker: Boolean
    ) : UserProfileUIAction

    class OnTryToCall(val userId: Long) : UserProfileUIAction

    class SetSuggestionsEnabled(val enabled: Boolean) : UserProfileUIAction

    class SetBirthdayFloorEnabled(val enabled: Boolean) : UserProfileUIAction

    class SubscribeSuggestion(
        val userId: Long,
        val isApprovedUser: Boolean,
        val topContentMaker: Boolean
    ) : UserProfileUIAction

    class UnsubscribeSuggestion(
        val userId: Long,
        val isApprovedUser: Boolean,
        val topContentMaker: Boolean
    ) : UserProfileUIAction

    class AddFriendSuggestion(
        val userId: Long,
        val isApprovedUser: Boolean,
        val topContentMaker: Boolean
    ) : UserProfileUIAction

    class RemoveFriendSuggestion(val userId: Long) : UserProfileUIAction

    object LogPeopleSelectedFromSuggestionUiAction : UserProfileUIAction

    class AddEvent(val where: AmplitudePropertyMapEventsCreateTapWhere) : UserProfileUIAction

    class OnNewPostClicked(val isWithImages: Boolean) : UserProfileUIAction

    data class BlockSuggestionById(val userId: Long) : UserProfileUIAction
    class OnSuggestionUserClicked(
        val isTopContentMaker: Boolean,
        val isApproved: Boolean,
        val hasMutualFriends: Boolean,
        val isSubscribed: Boolean,
        val toUserId: Long
    ) : UserProfileUIAction

    class OnSubscribeClicked(
        val isSubscribed: Boolean,
        val userId: Long,
        val friendStatus: Int,
        val approved: Boolean,
        val topContent: Boolean,
        val message: String
    ) : UserProfileUIAction

    class OnSubscribeRequestClicked(
        val isSubscribed: Boolean,
        val userId: Long,
        val friendStatus: Int,
        val approved: Boolean,
        val topContent: Boolean,
        val message: String
    ) : UserProfileUIAction

    class OnShowSuggestion(val isSuggestionShow: Boolean) : UserProfileUIAction

    object HandleNavigateSyncContactsUiAction : UserProfileUIAction
    object OnShowMoreSuggestionsClicked : UserProfileUIAction

    class OnFriendClicked(
        val friendStatus: Int = -1,
        val approved: Boolean = false,
        val influenecer: Boolean = false
    ) : UserProfileUIAction

    class OnSuggestionFriendClicked(
        val friendStatus: Int = -1,
        val approved: Boolean = false,
        val influenecer: Boolean = false
    ) : UserProfileUIAction

    class OnMomentClicked(
        val openedFrom: MomentClickOrigin,
        val existUserId: Long,
        val hasNewMoments: Boolean?
    ) : UserProfileUIAction

    class OnShowImage(
        val listPhotoEntity: List<GalleryPhotoEntity>,
        val position: Int,
        val isAvatarPhoto: Boolean = false
    ) : UserProfileUIAction

    class OnLogProfileEntrance(
        val where: String,
        val relationship: FriendRelationshipProperty,
        val approved: Boolean,
        val topContentMaker: Boolean,
        val countMutualAudience: Int,
        val haveVisibilityMutualAudience: Boolean
    ) : UserProfileUIAction


    object GetUserDataForScreenshotPopup : UserProfileUIAction

    data class HideVehicle(val vehicleId: String) : UserProfileUIAction
    data class ShowVehicle(val vehicleId: String) : UserProfileUIAction
    data class DeleteVehicle(val vehicleId: String) : UserProfileUIAction
}
