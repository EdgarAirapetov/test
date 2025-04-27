package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.PhotoModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import com.numplates.nomera3.modules.userprofile.ui.model.AccountStatusDetails
import com.numplates.nomera3.modules.userprofile.ui.model.AvatarDetails
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.LocationDetails
import com.numplates.nomera3.modules.userprofile.ui.model.SettingsFlags
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import javax.inject.Inject

class UserProfileUIMapper @Inject constructor() {

    fun mapDomainToUIModel(user: UserProfileModel, isMe: Boolean = false): UserProfileUIModel {
        user.profileVerified
        return UserProfileUIModel(
            userId = user.userId,
            birthday = user.birthday ?: 0,
            birthdayFlag = user.birthdayFlag ?: 0,
            gender = user.gender ?: -1,
            name = user.name ?: "",
            uniquename = user.uniquename,
            postsCount = user.postsCount ?: 0,
            subscribersCount = user.subscribersCount,
            photoCount = user.photosCount ?: 0,
            showOnMap = user.showOnMap ?: 0,
            avatarDetails = mapToAvatarDetails(user),
            photos = toGalleryItems(user.photos, isMe = isMe),
            accountDetails = mapAccountStatus(user),
            locationDetails = mapToUserLocation(user),
            friendStatus = when(user.friendStatus) {
                FriendStatus.FRIEND_STATUS_CONFIRMED.intStatus -> FriendStatus.FRIEND_STATUS_CONFIRMED
                FriendStatus.FRIEND_STATUS_INCOMING.intStatus -> FriendStatus.FRIEND_STATUS_INCOMING
                FriendStatus.FRIEND_STATUS_OUTGOING.intStatus -> FriendStatus.FRIEND_STATUS_OUTGOING
                else -> FriendStatus.FRIEND_STATUS_NONE
            },
            settingsFlags = mapToSettingsFlag(user),
            role = user.role ?: String.empty(),
            blacklistedByMe = user.blacklistedByMe.toBoolean(),
            blacklistedMe = user.blacklistedMe.toBoolean(),
            moments = user.moments,
            approved = user.approved.toBoolean(),
            topContentMaker = user.topContentMaker.toBoolean(),
            showFriendsAndSubscribers = user.showFriendsAndSubscribers.toBoolean(),
            isClosedProfile =  user.closedProfile.toBoolean(),
            profileVerified = user.profileVerified.toBoolean()
        )
    }

    private fun toGalleryItems(photos: List<PhotoModel>?, isMe: Boolean) = photos?.map { photo ->
        GalleryPhotoEntity(
            id = photo.id ?: 0, link = photo.link ?: "", isAdult = (photo.isAdult ?: false) && isMe.not()
        )
    } ?: emptyList()

    private fun mapToAvatarDetails(user: UserProfileModel) = AvatarDetails(
        avatarSmall = user.avatarSmall ?: "",
        avatarBig = user.avatarBig ?: "",
        avatarAnimation = user.avatarAnimation
    )

    private fun mapAccountStatus(user: UserProfileModel) = AccountStatusDetails(
        accountColor = user.accountColor ?: 0,
        accountType = user.accountType ?: 0,
        isAccountApproved = user.approved.toBoolean(),
        isAccountDeleted = user.profileDeleted.toBoolean(),
        isAccountBlocked = user.profileBlocked == 1,
        isTopContentMaker = user.topContentMaker.toBoolean()
    )

    private fun mapToUserLocation(user: UserProfileModel) = LocationDetails(
        cityName = user.coordinates?.cityName ?: "",
        countryName = user.coordinates?.countryName ?: "",
        longitude = user.coordinates?.longitude,
        latitude = user.coordinates?.latitude
    )

    private fun mapToSettingsFlag(user: UserProfileModel) = SettingsFlags(
        userCanChatMe = user.settingsFlags?.userCanChatMe.toBoolean(),
        userCanCallMe = user.settingsFlags?.userCanCallMe.toBoolean(),
        iCanGreet = user.settingsFlags?.iCanGreet.toBoolean(),
        iCanChat = user.settingsFlags?.iCanChat.toBoolean(),
        iCanCall = user.settingsFlags?.iCanCall.toBoolean(),
        canWriteAnonymousMessages = user.canWriteAnonymousMessages.toBoolean(),
        blacklistedByMe = user.blacklistedByMe.toBoolean(),
        blacklistedMe = user.blacklistedMe.toBoolean(),
        hideGender = user.hideGender.toBoolean(),
        hideBirthday = user.hideBirthday.toBoolean(),
        isHideRoadPosts = user.settingsFlags?.hideRoadPosts.toBoolean(),
        isSubscriptionOn = user.settingsFlags?.subscription_on.toBoolean(),
        isSubscriptionNotificationEnabled = user.settingsFlags?.subscription_notify.toBoolean(),
        notificationsOff = user.settingsFlags?.notificationsOff.toBoolean(),
        isSubscribedToMe = user.settingsFlags?.subscribedToMe.toBoolean(),
        friendStatus = user.friendStatus ?: 0
    )
}
