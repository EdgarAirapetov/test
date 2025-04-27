package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.meera.core.extensions.toBoolean
import com.meera.core.utils.isBirthdayToday
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.holidays.data.mapper.toGiftItem
import com.numplates.nomera3.modules.userprofile.domain.maper.DisableLastSeparatorMapper
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.GiftModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.GroupModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.PhotoModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserVehicleModel
import com.numplates.nomera3.modules.userprofile.ui.entity.BannerType
import com.numplates.nomera3.modules.userprofile.ui.entity.CoordinatesUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.GalleryPhotoEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.GiftUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.GroupUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.MutualSubscribersUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionsFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.SubscribersFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityBannerFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityBirthdayFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityClosedProfileFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityFriendSubscribeFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGalleryFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGarageFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGiftsFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGroupFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityHolidayFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityMapFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityRoadFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityUpdateBtn
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.presentation.model.MutualUser
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class ProfileUIListMapper @Inject constructor() {
    fun map(
        isMe: Boolean,
        needToUpdateApp: Boolean,
        profile: UserProfileModel,
        profileSuggestions: List<ProfileSuggestionUiModels>? = null
    ): List<UserUIEntity> {
        val userType = createAccountTypeEnum(profile.accountType)
        val disableLastSeparatorMapper = DisableLastSeparatorMapper()
        val result = mutableListOf<UserUIEntity>()
        val isBlockedOrDeleted = checkBlockAndDeletion(profile, result, userType)
        if (isBlockedOrDeleted) return result
        if (isMe) addMyFloors(profile, result, userType, needToUpdateApp)
        else addNotMyFloors(profile, result, userType, profileSuggestions)
        result.add(getRoadFloor(profile, isMe, userType))
        return if (!isMe && profile.postsCount == 0) disableLastSeparatorMapper.map(result)
        else result
    }

    private fun addNotMyFloors(
        profile: UserProfileModel,
        result: MutableList<UserUIEntity>,
        userType: AccountTypeEnum,
        profileSuggestions: List<ProfileSuggestionUiModels>?
    ) {
        result.add(getFriendsSubscribeFloor(profile, userType))
        val blacklistedByMe = profile.blacklistedByMe ?: 0
        addProfileSuggestionsBlockIfNotBlackListByMe(profile, blacklistedByMe, userType, profileSuggestions, result)
        addSubscriptionFloorIfNotBlackListByMe(profile, blacklistedByMe, userType, result)
        addMutualSubscribersIfNotBlackListByMe(profile, blacklistedByMe, result, userType)

        var isShownBirthdayFloor = false
        val needToShowBirthdayBanner = profile.birthdayFlag != null &&
            profile.showBirthdayButton == 1L &&
            isBirthdayToday(profile.birthdayFlag)
        if (needToShowBirthdayBanner) {
            result.add(UserEntityBirthdayFloor())
            isShownBirthdayFloor = true
        }
        var needToShownGiftsFloor = !profile.gifts.isNullOrEmpty()
        if (!isShownBirthdayFloor) {
            needToShownGiftsFloor = handleBannerOrHoliday(profile, result, userType, needToShownGiftsFloor)
        }



        if (profile.photos?.isEmpty() == false) {
            val listPhotos = profile.photos
            result.add(getGalleryFloor(listPhotos, profile, userType, false))
        }
        if (profile.showOnMap == 1 && profile.userRole != UserRole.SUPPORT_USER) {
            result.add(getMapFloor(profile, userType, false))
        }
        if (profile.vehicles?.isEmpty() == false) result.add(
            getGarageFloor(profile.vehicles, profile, userType, false)
        )
        if (needToShownGiftsFloor) result.add(getGiftsFloor(profile, userType, false))
        if (profile.closedProfile.toBoolean() && isStatusNotConfirmed(profile)) {
            result.add(UserEntityClosedProfileFloor())
        }
    }

    private fun isStatusNotConfirmed(profile: UserProfileModel) =
        profile.friendStatus != FriendStatus.FRIEND_STATUS_CONFIRMED.intStatus

    private fun addProfileSuggestionsBlockIfNotBlackListByMe(
        profile: UserProfileModel,
        blacklistedByMe: Int,
        userType: AccountTypeEnum,
        profileSuggestions: List<ProfileSuggestionUiModels>?,
        result: MutableList<UserUIEntity>
    ) {
        if (profileSuggestions.isNullOrEmpty()) return
        if (!blacklistedByMe.toBoolean()) {
            val profileSuggestionsFloor = ProfileSuggestionsFloorUiEntity(
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

    private fun addMyFloors(
        profile: UserProfileModel,
        result: MutableList<UserUIEntity>,
        userType: AccountTypeEnum,
        needToUpdateApp: Boolean
    ) {
        if (needToUpdateApp) result.add(UserEntityUpdateBtn())
        result.add(getSubscribersFloor(profile, userType, true))
        val photos = profile.photos ?: emptyList()
        result.add(getGalleryFloor(photos, profile, userType, true))
        result.add(getMapFloor(profile, userType, true))
        val vehicles = profile.vehicles ?: mutableListOf()
        result.add(getGarageFloor(vehicles, profile, userType, true))
        result.add(getGiftsFloor(profile, userType, true))
        result.add(getGroupFloor(profile, userType))
    }

    private fun handleBannerOrHoliday(
        profile: UserProfileModel,
        result: MutableList<UserUIEntity>,
        userType: AccountTypeEnum,
        needToShownGiftsFloor: Boolean
    ): Boolean {
        if (profile.holidayProduct != null) {
            profile.holidayProduct.let { product ->
                val holidayFloor = UserEntityHolidayFloor(
                    userType.value == INetworkValues.ACCOUNT_TYPE_VIP, product.toGiftItem()
                )
                result.add(holidayFloor)
            }
        } else if (profile.gifts.isNullOrEmpty()) {
            val bannerGift = UserEntityBannerFloor(BannerType.BANNER_TYPE_GIFT, userType)
            result.add(bannerGift)
            return false
        }
        return needToShownGiftsFloor
    }

    private fun getFriendsSubscribeFloor(
        profile: UserProfileModel, userType: AccountTypeEnum
    ) = UserEntityFriendSubscribeFloor(
        isUserBlacklisted = profile.blacklistedByMe.toBoolean(),
        userId = profile.userId,
        friendStatus = profile.friendStatus ?: FRIEND_STATUS_NONE,
        isSubscribed = profile.settingsFlags?.subscription_on.toBoolean(),
        userStatus = userType,
        approved = profile.approved.toBoolean(),
        topContentMaker = profile.topContentMaker.toBoolean(),
        name = profile.name.orEmpty()
    )

    private fun getGroupFloor(
        profile: UserProfileModel,
        userType: AccountTypeEnum,
    ) = UserEntityGroupFloor(
        groups = toGroupsUIItems(profile.groups),
        groupCount = profile.groupsCount ?: profile.groups?.size ?: 0,
        userTypeEnum = userType
    )

    private fun getGalleryFloor(
        listPhotos: List<PhotoModel>, profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserEntityGalleryFloor(
        photoCount = profile.photosCount ?: listPhotos.size,
        listPhotoEntity = toGalleryItems(listPhotos, isMe),
        accountTypeEnum = userType,
        isMineGallery = isMe,
        isLoading = false
    )

    private fun getMapFloor(
        profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserEntityMapFloor(
        accountTypeEnum = userType,
        accountColor = profile.accountColor,
        userAvatarSmall = profile.avatarSmall,
        isMe = isMe,
        distance = profile.distance,
        coordinates = CoordinatesUIModel(profile.coordinates?.latitude, profile.coordinates?.longitude)
    )

    private fun getGarageFloor(
        listVehicles: List<UserVehicleModel>, profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserEntityGarageFloor(
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

    private fun getGiftsFloor(
        profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ) = UserEntityGiftsFloor(
        listGiftEntity = toGiftUIItems(profile.gifts),
        giftsCount = profile.giftsCount ?: 0,
        giftsNewCount = profile.giftsNewCount ?: 0,
        accountTypeEnum = userType,
        isMe = isMe
    )

    private fun getSubscribersFloor(
        profile: UserProfileModel, userType: AccountTypeEnum, isMe: Boolean
    ): SubscribersFloorUiEntity {
        val countMutualFriends = profile.mutualUsers?.userSimple?.size ?: 0
        val countMutualOthers = profile.mutualUsers?.moreCount ?: 0
        return SubscribersFloorUiEntity(
            userStatus = userType,
            subscribersCount = profile.subscribersCount,
            subscriptionCount = profile.subscriptionCount,
            friendsCount = profile.friendsCount?.toLong() ?: 0L,
            mutualFriendsAndSubscribersCount = countMutualFriends + countMutualOthers,
            friendsRequestCount = profile.friendsRequestCount?.toLong() ?: 0L,
            showFriendsSubscribers = isAllowShowFriendsAndSubscribers(profile),
            isMe = isMe
        )
    }

    private fun getRoadFloor(
        profile: UserProfileModel, isMe: Boolean, userType: AccountTypeEnum
    ) = UserEntityRoadFloor(
        postCount = profile.postsCount ?: 0, isMe = isMe, userTypeEnum = userType
    )


    private fun checkBlockAndDeletion(
        profile: UserProfileModel, list: MutableList<UserUIEntity>, userType: AccountTypeEnum
    ): Boolean {
        if (profile.profileDeleted == 1) return true
        if (profile.blacklistedMe == 1) {
            val blackListedBannerFloor = UserEntityBannerFloor(
                userType = userType, bannerType = BannerType.BANNER_TYPE_BLOCKED_ME
            )
            list.add(blackListedBannerFloor)
            return true
        }
        return false
    }

    private fun toGalleryItems(photos: List<PhotoModel>?, isMe: Boolean) = photos?.map { photo ->
        GalleryPhotoEntity(
            id = photo.id ?: 0, link = photo.link ?: "", isAdult = (photo.isAdult ?: false) && isMe.not()
        )
    } ?: emptyList()


    private fun toGiftUIItems(gifts: List<GiftModel>?) = gifts?.map { gift ->
        GiftUIModel(
            giftId = gift.id,
            image = gift.imageSmall,
            typeId = gift.typeId,
            isViewed = gift.isViewed,
            isReceived = gift.isReceived
        )
    } ?: emptyList()


    private fun toGroupsUIItems(groups: List<GroupModel>?) = groups?.map { group ->
        GroupUIModel(
            id = group.groupId, name = group.name, avatar = group.avatar, countMembers = group.countMembers
        )
    } ?: emptyList()


    private fun addSubscriptionFloorIfNotBlackListByMe(
        profile: UserProfileModel, blacklistedByMe: Int, userType: AccountTypeEnum, result: MutableList<UserUIEntity>
    ) {
        if (!blacklistedByMe.toBoolean()) {
            result.add(getSubscribersFloor(profile, userType, false))
        }
    }

    private fun addMutualSubscribersIfNotBlackListByMe(
        profile: UserProfileModel, blacklistedByMe: Int, result: MutableList<UserUIEntity>, userType: AccountTypeEnum
    ) {
        profile.mutualUsers?.let { mutualUsers ->
            if (!blacklistedByMe.toBoolean() && !mutualUsers.userSimple.isNullOrEmpty() &&
                isAllowShowFriendsAndSubscribers(profile)
            ) {
                val user = mutualUsers.userSimple
                val uiEntity = MutualSubscribersUiEntity(
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

    private fun isAllowShowFriendsAndSubscribers(profile: UserProfileModel): Boolean {
        return profile.showFriendsAndSubscribers == SettingsUserTypeEnum.ALL.key ||
            isAllowShowSubscribersByFriendPrivacy(profile)
    }

    private fun isAllowShowSubscribersByFriendPrivacy(profile: UserProfileModel): Boolean {
        return profile.showFriendsAndSubscribers == SettingsUserTypeEnum.FRIENDS.key &&
            profile.friendStatus == FRIEND_STATUS_CONFIRMED
    }
}
