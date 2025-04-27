package com.numplates.nomera3.modules.userprofile.domain.model.usermain

import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel

data class UserProfileModel(
    val userId: Long,
    val birthday: Long? = 0L,
    val birthdayFlag: Long? = 0L,
    val showBirthdayButton: Long? = 0L,
    val avatarBig: String? = "",
    val avatarSmall: String? = "",
    val accountColor: Int? = 0,
    val gender: Int? = -1,
    val name: String? = "",
    val accountType: Int? = 0,
    val profileRating: Int? = 0,
    val blacklistedByMe: Int?,
    val blacklistedMe: Int?,
    val friendStatus: Int? = 0,
    val friendsCount: Int? = 0,
    val vehiclesCount: Int? = 0,
    val groupsCount: Int? = 0,
    val photosCount: Int? = 0,
    val postsCount: Int? = 0,
    val giftsCount: Int? = 0,
    val giftsNewCount: Int? = 0,
    val showOnMap: Int? = 0,
    val closedProfile: Int? = 0,
    val canWriteAnonymousMessages: Int? = 0,
    val accountTypeExpiration: Long? = -1,
    val profileDeleted: Int? = 0,
    val profileBlocked: Int? = 0,
    val profileVerified: Int? = 0,
    val showBirthday: Int? = 0,
    val mapState: Int?,
    val isAnonym: Int?,
    val membershipType: Int?,
    val membershipStatus: Int?,
    var hideBirthday: Int?,
    var hideGender: Int?,
    var subscriptionCount: Long = 0,
    var subscribersCount: Long = 0,
    var friendsRequestCount: Int? = 0,
    var deletedAt: Long? = null,
    var uniquename: String,
    var distance: Int? = null,
    val isSystemAdmin: Boolean = false,
    var avatarAnimation: String? = "",
    val complete: Int? = 0,
    val phoneNumber: String? = null,
    val email: String? = null,
    val approved: Int = 0,
    val topContentMaker: Int,
    val showFriendsAndSubscribers: Int? = 0,
    val profileStatus: String? = null,
    val registrationDate: Long? = null,
    val holidayProduct: ProductHolidayModel? = null,
    val vehicles: List<UserVehicleModel>? = mutableListOf(),
    val gifts: List<GiftModel>? = mutableListOf(),
    val photos: List<PhotoModel>? = mutableListOf(),
    var settingsFlags: UserSettingsFlagsModel? = null,
    val coordinates: LocationModel?,
    val groups: List<GroupModel>? = mutableListOf(),
    val mutualUsers: MutualUsersModel? = null,
    val role: String? = null,
    val eventCount: Int = 0,
    val isProfileFilled: Boolean,
    val moments: UserMomentsModel?
) {
    val userRole: UserRole
        get() = when (role) {
            UserRole.USER.value -> UserRole.USER
            UserRole.ANNOUNCE_USER.value -> UserRole.ANNOUNCE_USER
            UserRole.SUPPORT_USER.value -> UserRole.SUPPORT_USER
            UserRole.SYSTEM_ADMIN.value -> UserRole.SYSTEM_ADMIN
            else -> UserRole.USER
        }
}

