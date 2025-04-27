package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.isBirthdayToday
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.PhotoModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserVehicleModel
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.presentation.model.MutualUser
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class MeeraProfileUIListMapper @Inject constructor() {
    fun map(
        isMe: Boolean,
        needToUpdateApp: Boolean,
        profile: UserProfileModel,
        profileSuggestions: List<ProfileSuggestionUiModels>? = null,
        birthdayVisible: Boolean,
        isSnippetCollapsed: Boolean
    ): List<UserInfoRecyclerData> {
        val userType = createAccountTypeEnum(profile.accountType)
        val result = mutableListOf<UserInfoRecyclerData>()
        val isBlockedOrDeleted = checkBlockAndDeletion(profile, result)
        if (isBlockedOrDeleted) return result
        if (isMe) addMyFloors(profile, result, userType, needToUpdateApp, isSnippetCollapsed)
        else addNotMyFloors(
            profile = profile,
            result = result,
            userType = userType,
            profileSuggestions = profileSuggestions,
            isSuggestionShowed = profileSuggestions != null,
            birthdayVisible = birthdayVisible,
            isSnippetCollapsed = isSnippetCollapsed
        )
        result.add(getRoadFloor(profile, isMe, userType))
        return result
    }

    private fun addNotMyFloors(
        profile: UserProfileModel,
        result: MutableList<UserInfoRecyclerData>,
        userType: AccountTypeEnum,
        profileSuggestions: List<ProfileSuggestionUiModels>? = null,
        isSuggestionShowed:Boolean,
        birthdayVisible: Boolean,
        isSnippetCollapsed: Boolean
    ) {
        val blacklistedByMe = profile.blacklistedByMe ?: 0
        val blacklistedMe = profile.blacklistedMe ?: 0
        addSubscriptionFloorIfNotBlackListByMe(
            profile = profile,
            blacklistedByMe = blacklistedByMe,
            blacklistedMe = blacklistedMe,
            userType = userType,
            result = result
        )
        if (!isSnippetCollapsed) {
            if (blacklistedByMe.toBoolean()) {
                result.add(UserInfoRecyclerData.UserEntityBlockedProfileFloor)
                return
            }
            if (!blacklistedByMe.toBoolean() && !blacklistedMe.toBoolean()) {
                result.add(getFriendsSubscribeFloor(profile, userType,isSuggestionShowed))
            }

            addProfileSuggestionsBlockIfNotBlackListByMe(
                profile = profile,
                blacklistedByMe = blacklistedByMe,
                blacklistedMe = blacklistedMe,
                userType = userType,
                profileSuggestions = profileSuggestions,
                result = result
            )
            addMutualSubscribersIfNotBlackListByMe(
                profile = profile,
                blacklistedByMe = blacklistedByMe,
                blacklistedMe = blacklistedMe,
                result = result,
                userType = userType
            )

            val needToShowBirthdayBanner = profile.birthdayFlag != null &&
                profile.showBirthdayButton == 1L &&
                isBirthdayToday(profile.birthdayFlag) && birthdayVisible
            if (needToShowBirthdayBanner) {
                result.add(UserInfoRecyclerData.UserEntityBirthdayFloor)
            }

            if (blacklistedMe.toBoolean().not() && profile.photos?.isNotEmpty().isTrue()) {
                val listPhotos = profile.photos
                result.add(getGalleryFloor(listPhotos, profile, userType, false))
            }
            if (profile.vehicles?.isEmpty() == false && blacklistedMe.toBoolean().not()) {
                result.add(getGarageFloor(profile.vehicles, profile, userType, false))
            }
            if (profile.closedProfile.toBoolean() && isStatusNotConfirmed(profile)) {
                result.add(UserInfoRecyclerData.UserEntityClosedProfileFloor)
            }
            if (blacklistedMe.toBoolean()) {
                result.add(UserInfoRecyclerData.UserEntityBlockedMeProfileFloor)
            }
        }
    }


    private fun addMyFloors(
        profile: UserProfileModel,
        result: MutableList<UserInfoRecyclerData>,
        userType: AccountTypeEnum,
        needToUpdateApp: Boolean,
        isSnippetCollapsed: Boolean
    ) {
        result.add(getSubscribersFloor(profile, userType, true))
        if (!isSnippetCollapsed){
            if (needToUpdateApp) result.add(UserInfoRecyclerData.UserEntityUpdateBtn)
            val photos = profile.photos ?: emptyList()
            result.add(getGalleryFloor(photos, profile, userType, true))
            val vehicles = profile.vehicles ?: mutableListOf()
            result.add(getGarageFloor(vehicles, profile, userType, true))
        }
    }

    private fun isStatusNotConfirmed(profile: UserProfileModel) =
        profile.friendStatus != FriendStatus.FRIEND_STATUS_CONFIRMED.intStatus

    private fun getGarageFloor(
        listVehicles: List<UserVehicleModel>, profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserInfoRecyclerData.UserInfoGarageFloorRecyclerData(
        vehicleCount = profile.vehiclesCount ?: listVehicles.size,
        listVehicles = mapVehicles(listVehicles),
        isMe = isMe,
        accountTypeEnum = userType,
        userColor = profile.accountColor
    )

    private fun mapVehicles(vehicles: List<UserVehicleModel>): List<VehicleUIModel> {
        return vehicles.map { vehicle ->
            VehicleUIModel(
                vehicleId = vehicle.vehicleId,
                brandLogo = vehicle.brandLogo,
                avatarSmall = vehicle.avatarSmall,
                hasNumber = vehicle.hasNumber,
                brandName = vehicle.brandName,
                modelName = vehicle.modelName,
                number = vehicle.number,
                typeId = vehicle.typeId,
                countryId = vehicle.countryId,
                hidden = false
            )
        }
    }

    private fun addMutualSubscribersIfNotBlackListByMe(
        profile: UserProfileModel,
        blacklistedByMe: Int,
        blacklistedMe: Int,
        result: MutableList<UserInfoRecyclerData>,
        userType: AccountTypeEnum
    ) {
        profile.mutualUsers?.let { mutualUsers ->
            if (!blacklistedByMe.toBoolean() &&
                !blacklistedMe.toBoolean() &&
                !mutualUsers.userSimple.isNullOrEmpty() &&
                isAllowShowFriendsAndSubscribers(profile)
            ) {
                val user = mutualUsers.userSimple
                val uiEntity = UserInfoRecyclerData.MutualSubscribersUiEntity(
                    mutualSubscribersFriends = user.map { toMutualUser(it) },
                    moreCount = mutualUsers.moreCount ?: 0,
                    userType = userType
                )
                result.add(uiEntity)
            }
        }
    }

    private fun toMutualUser(user: UserSimpleModel): MutualUser = MutualUser(
        id = user.userId, name = user.name ?: "", avatarSmall = user.avatarSmall ?: ""
    )

    private fun getFriendsSubscribeFloor(
        profile: UserProfileModel, userType: AccountTypeEnum,isSuggestionShowed:Boolean
    ): UserInfoRecyclerData {
        return UserInfoRecyclerData.UserEntityFriendSubscribeFloor(
            isUserBlacklisted = profile.blacklistedByMe.toBoolean(),
            userId = profile.userId,
            friendStatus = profile.friendStatus ?: FRIEND_STATUS_NONE,
            isSubscribed = profile.settingsFlags?.subscription_on.toBoolean(),
            userStatus = userType,
            approved = profile.approved.toBoolean(),
            topContentMaker = profile.topContentMaker.toBoolean(),
            name = profile.name.orEmpty(),
            isSuggestionShowed = isSuggestionShowed
        )
    }

    private fun getSubscribersFloor(
        profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ): UserInfoRecyclerData {
        val countMutualFriends = profile.mutualUsers?.userSimple?.size ?: 0
        val countMutualOthers = profile.mutualUsers?.moreCount ?: 0

        return UserInfoRecyclerData.SubscribersFloorUiEntity(
            userStatus = userType,
            subscribersCount = profile.subscribersCount,
            subscriptionCount = profile.subscriptionCount,
            friendsCount = profile.friendsCount?.toLong() ?: 0L,
            mutualFriendsAndSubscribersCount = countMutualFriends + countMutualOthers,
            friendsRequestCount = profile.friendsRequestCount?.toLong() ?: 0L,
            showFriendsSubscribers = isAllowShowFriendsAndSubscribers(profile),
            isMe = isMe,
            city = profile.coordinates?.cityName ?: "",
            country = profile.coordinates?.countryName ?: "",
        )
    }

    private fun getGalleryFloor(
        listPhotos: List<PhotoModel>?, profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserInfoRecyclerData.UserEntityGalleryFloor(
        photoCount = profile.photosCount ?: listPhotos?.size ?: 0,
        listPhotoEntity = toGalleryItems(listPhotos, isMe),
        accountTypeEnum = userType,
        isMineGallery = isMe,
        isLoading = false
    )

    private fun toGalleryItems(photos: List<PhotoModel>?, isMe: Boolean) = photos?.map { photo ->
        GalleryPhotoEntity(
            id = photo.id ?: 0, link = photo.link ?: "", isAdult = (photo.isAdult ?: false) && isMe.not()
        )
    } ?: emptyList()

    private fun addSubscriptionFloorIfNotBlackListByMe(
        profile: UserProfileModel,
        blacklistedByMe: Int,
        blacklistedMe: Int,
        userType: AccountTypeEnum,
        result: MutableList<UserInfoRecyclerData>
    ) {
        if (!blacklistedByMe.toBoolean() && !blacklistedMe.toBoolean()) {
            result.add(getSubscribersFloor(profile, userType, false))
        }
    }

    private fun addProfileSuggestionsBlockIfNotBlackListByMe(
        profile: UserProfileModel,
        blacklistedByMe: Int,
        blacklistedMe: Int,
        userType: AccountTypeEnum,
        profileSuggestions: List<ProfileSuggestionUiModels>?,
        result: MutableList<UserInfoRecyclerData>
    ) {
        if (profileSuggestions.isNullOrEmpty()) return
        if (!blacklistedByMe.toBoolean() && !blacklistedMe.toBoolean()) {
            val profileSuggestionsFloor = UserInfoRecyclerData.ProfileSuggestionFloor(
                userType = userType,
                suggestions = profileSuggestions.map {
                    val isVip = profile.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP.value
                    if (it is ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
                        it.isVip = isVip
                    } else if (it is ProfileSuggestionUiModels.SuggestionSyncContactUiModel) {
                        it.isUserVip = isVip
                    }
                    it
                }
            )
            result.add(profileSuggestionsFloor)
        }
    }

    private fun isAllowShowFriendsAndSubscribers(profile: UserProfileModel): Boolean {
        return profile.showFriendsAndSubscribers == SettingsUserTypeEnum.ALL.key ||
            isAllowShowSubscribersByFriendPrivacy(profile)
    }

    private fun isAllowShowSubscribersByFriendPrivacy(profile: UserProfileModel): Boolean {
        return profile.showFriendsAndSubscribers == SettingsUserTypeEnum.FRIENDS.key &&
            profile.friendStatus == FRIEND_STATUS_CONFIRMED
    }

    private fun getRoadFloor(
        profile: UserProfileModel, isMe: Boolean, userType: AccountTypeEnum
    ) = UserInfoRecyclerData.UserEntityRoadFloor(
        postCount = profile.postsCount ?: 0, isMe = isMe, userTypeEnum = userType
    )

    private fun checkBlockAndDeletion(
        profile: UserProfileModel,
        list: MutableList<UserInfoRecyclerData>,
    ): Boolean {
        if (profile.profileDeleted == 1) return true
        if (profile.blacklistedMe == 1) {
            val blackListedBannerFloor = UserInfoRecyclerData.UserEntityBlockedMeProfileFloor
            list.add(blackListedBannerFloor)
            return true
        }
        return false
    }
}
