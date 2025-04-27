package com.numplates.nomera3.modules.userprofile.ui.model

import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity

data class UserProfileUIModel(
    val userId: Long,
    val birthday: Long,
    val birthdayFlag: Long,
    val gender: Int,
    val name: String,
    val uniquename: String,
    val postsCount: Int,
    val subscribersCount: Long,
    val photoCount: Int,
    val showOnMap: Int,
    val avatarDetails: AvatarDetails,
    val photos: List<GalleryPhotoEntity>,
    val accountDetails: AccountStatusDetails,
    val locationDetails: LocationDetails,
    val settingsFlags: SettingsFlags,
    val friendStatus: FriendStatus,
    val role: String,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean,
    val moments: UserMomentsModel?,
    val approved: Boolean,
    val topContentMaker: Boolean,
    val showFriendsAndSubscribers: Boolean,
    val isClosedProfile: Boolean,
    val profileVerified: Boolean
)

data class SettingsFlags(
    val friendStatus: Int,
    val userCanChatMe: Boolean,
    val userCanCallMe: Boolean,
    val iCanGreet: Boolean,
    val iCanChat: Boolean,
    val iCanCall: Boolean,
    val canWriteAnonymousMessages: Boolean,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean,
    val hideGender: Boolean,
    val hideBirthday: Boolean,
    val isHideRoadPosts: Boolean,
    val isSubscriptionOn: Boolean,
    val isSubscribedToMe: Boolean,
    val isSubscriptionNotificationEnabled: Boolean,
    val notificationsOff: Boolean
)

data class AvatarDetails(
    val avatarBig: String, val avatarSmall: String, val avatarAnimation: String?
)

data class AccountStatusDetails(
    val accountColor: Int,
    val accountType: Int,
    val isAccountApproved: Boolean,
    val isAccountDeleted: Boolean,
    val isAccountBlocked: Boolean,
    val isTopContentMaker: Boolean
)

data class LocationDetails(
    val cityName: String, val countryName: String, val latitude: Double?, val longitude: Double?
)

enum class FriendStatus(val intStatus: Int) {
    FRIEND_STATUS_NONE(com.numplates.nomera3.FRIEND_STATUS_NONE),
    FRIEND_STATUS_OUTGOING(com.numplates.nomera3.FRIEND_STATUS_OUTGOING),
    FRIEND_STATUS_CONFIRMED(com.numplates.nomera3.FRIEND_STATUS_CONFIRMED),
    FRIEND_STATUS_INCOMING(com.numplates.nomera3.FRIEND_STATUS_INCOMING)
}

val UserProfileUIModel.userRole: UserRole
    get() = when (role) {
        UserRole.USER.value -> UserRole.USER
        UserRole.ANNOUNCE_USER.value -> UserRole.ANNOUNCE_USER
        UserRole.SUPPORT_USER.value -> UserRole.SUPPORT_USER
        UserRole.SYSTEM_ADMIN.value -> UserRole.SYSTEM_ADMIN
        else -> UserRole.USER
    }
