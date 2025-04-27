package com.numplates.nomera3.modules.userprofile.domain.maper

import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.chatmembers.UserEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.userprofile.UserSettingsFlags
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileBlockData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileMainData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileSettingsData
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.UserUpdateModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType
import com.numplates.nomera3.modules.userprofile.ui.entity.BannerType
import com.numplates.nomera3.modules.userprofile.ui.entity.MutualSubscribersUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionsFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.SubscribersFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityBannerFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityDefaultSkeletonFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityFriendSubscribeFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGalleryFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGarageFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGiftsFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGroupFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityMapFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityRoadFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntitySubscribeSkeletonFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import timber.log.Timber
import java.util.Date

fun UserSnippetModel.toUserProfileUIList(): List<UserUIEntity> {

    val disableLastSeparatorMapper = DisableLastSeparatorMapper()
    val result = mutableListOf<UserUIEntity>()

    if (profileDeleted) return emptyList()

    if (blacklistedMe) {
        val blackListedBannerFloor = UserEntityBannerFloor(
            userType = accountType,
            bannerType = BannerType.BANNER_TYPE_BLOCKED_ME
        )
        result.add(blackListedBannerFloor)
        return result
    }

    val friendSubscribeFloor = UserEntityFriendSubscribeFloor(
        isUserBlacklisted = blacklistedByMe,
        userId = uid,
        friendStatus = friendStatus,
        isSubscribed = subscriptionOn,
        userStatus = accountType,
        approved = this.approved,
        topContentMaker = this.topContentMaker,
        name = name.orEmpty()
    )
    result.add(friendSubscribeFloor)

    result.add(UserEntityDefaultSkeletonFloor())
    result.add(UserEntityDefaultSkeletonFloor())

    return disableLastSeparatorMapper.map(result)
}

fun MapUserUiModel.toUserProfileUIList(): List<UserUIEntity> {

    val disableLastSeparatorMapper = DisableLastSeparatorMapper()
    val result = mutableListOf<UserUIEntity>()

    if (blacklistedMe) {
        val blackListedBannerFloor = UserEntityBannerFloor(
            userType = accountType,
            bannerType = BannerType.BANNER_TYPE_BLOCKED_ME
        )
        result.add(blackListedBannerFloor)
        return result
    }

    result.add(UserEntitySubscribeSkeletonFloor())
    result.add(UserEntityDefaultSkeletonFloor())
    result.add(UserEntityDefaultSkeletonFloor())

    return disableLastSeparatorMapper.map(result)
}

fun UserProfileUIModel.toUserChat(greetingAlreadySent: Boolean = false): UserChat {
    val iCanGreet = if (greetingAlreadySent) false else this.settingsFlags.iCanGreet
    return UserChat(
        userId = userId,
        name = name,
        avatarSmall = avatarDetails.avatarSmall,
        birthDate = birthday,
        canWriteAnonymousMessage = settingsFlags.canWriteAnonymousMessages.toInt(),
        blacklistedByMe = settingsFlags.blacklistedByMe.toInt(),
        blacklistedMe = settingsFlags.blacklistedMe.toInt(),
        settingsFlags = UserSettingsFlags(
            iCanChat = settingsFlags.iCanChat.toInt(),
            userCanChatMe = settingsFlags.userCanChatMe.toInt(),
            iCanCall = settingsFlags.iCanCall.toInt(),
            userCanCallMe = settingsFlags.userCanCallMe.toInt(),
            notificationsOff = settingsFlags.notificationsOff.toInt(),
            subscription_on = settingsFlags.isSubscriptionOn.toInt(),
            subscribedToMe = settingsFlags.isSubscribedToMe.toInt(),
            friendStatus = friendStatus.intStatus,
            iCanGreet = iCanGreet.toInt(),
        ),
        role = role
    )
}

fun UserProfileUIModel.toChatInitUserProfile(): ChatInitProfileData {
    return ChatInitProfileData(
        mainInfo = ChatInitProfileMainData(
            userId = userId,
            name = name,
            avatar = avatarDetails.avatarSmall,
            birthDate = birthday,
            role = role
        ),
        blockInfo = ChatInitProfileBlockData(
            blacklistedByMe = settingsFlags.blacklistedByMe.toInt(),
            blacklistedMe = settingsFlags.blacklistedMe.toInt()
        ),
        settings = ChatInitProfileSettingsData(
            iCanChat = settingsFlags.iCanChat.toInt(),
            userCanChatMe = settingsFlags.userCanChatMe.toInt(),
            iCanCall = settingsFlags.iCanCall.toInt(),
            userCanCallMe = settingsFlags.userCanCallMe.toInt(),
            notificationsOff = settingsFlags.notificationsOff.toInt(),
            subscriptionOn = settingsFlags.isSubscriptionOn.toInt(),
            subscribedToMe =  settingsFlags.isSubscribedToMe.toInt(),
            iCanGreet = settingsFlags.iCanGreet.toInt(),
            friendStatus = friendStatus.intStatus,
        ),
        moments = moments
    )
}

fun UserProfileModel.toChatInitUserProfile(): ChatInitProfileData {
    return ChatInitProfileData(
        mainInfo = ChatInitProfileMainData(
            userId = userId,
            name = name,
            avatar = avatarSmall,
            birthDate = birthday,
            role = role
        ),
        blockInfo = ChatInitProfileBlockData(
            blacklistedByMe = blacklistedByMe,
            blacklistedMe = blacklistedMe
        ),
        settings = ChatInitProfileSettingsData(
            iCanChat = settingsFlags?.iCanChat,
            userCanChatMe = settingsFlags?.userCanChatMe,
            iCanCall = settingsFlags?.iCanCall,
            userCanCallMe = settingsFlags?.userCanCallMe,
            notificationsOff = settingsFlags?.notificationsOff,
            subscriptionOn = settingsFlags?.subscription_on,
            subscribedToMe =  settingsFlags?.subscribedToMe,
            iCanGreet = settingsFlags?.iCanGreet,
            friendStatus = friendStatus,
        ),
        moments = moments
    )
}

fun UserUIEntity.compare(other: UserUIEntity): Boolean {
    if (type != other.type) return false

    when (other.type) {
        UserProfileAdapterType.UPDATE_BTN ->
            return true

        UserProfileAdapterType.SUBSCRIBERS_FLOOR -> {
            val otherSubscribersFloor = other as? SubscribersFloorUiEntity
            val thisSubscribersFloor = this as? SubscribersFloorUiEntity

            return (otherSubscribersFloor?.friendsCount == thisSubscribersFloor?.friendsCount
                    && otherSubscribersFloor?.friendsRequestCount == thisSubscribersFloor?.friendsRequestCount
                    && otherSubscribersFloor?.subscribersCount == thisSubscribersFloor?.subscribersCount
                    && otherSubscribersFloor?.subscriptionCount == thisSubscribersFloor?.subscriptionCount
                    && otherSubscribersFloor?.userStatus == thisSubscribersFloor?.userStatus)
                    && otherSubscribersFloor?.showFriendsSubscribers == thisSubscribersFloor?.showFriendsSubscribers
                    && otherSubscribersFloor?.isMe == thisSubscribersFloor?.isMe
        }

        UserProfileAdapterType.BANNER_FLOOR -> {
            val otherUser = other as? UserEntityBannerFloor
            val thisUser = this as? UserEntityBannerFloor

            return (otherUser?.bannerType == thisUser?.bannerType
                    && otherUser?.userType == thisUser?.userType)
        }


        UserProfileAdapterType.GALLERY_FLOOR -> {
            try {
                val otherUser = other as? UserEntityGalleryFloor
                val thisUser = this as? UserEntityGalleryFloor
                if (otherUser?.photoCount != thisUser?.photoCount) return false
                otherUser?.listPhotoEntity?.forEachIndexed { index, photoEntity ->
                    if (thisUser?.listPhotoEntity?.get(index)?.link != photoEntity.link) return false
                }

                return (otherUser?.isLoading == thisUser?.isLoading
                        && otherUser?.isMineGallery == thisUser?.isMineGallery
                        && otherUser?.accountTypeEnum == thisUser?.accountTypeEnum)
            } catch (e: Exception) {
                 Timber.e(e)
                return false
            }
        }

        UserProfileAdapterType.GROUPS_FLOOR -> {
            val otherUser = other as? UserEntityGroupFloor
            val thisUser = this as? UserEntityGroupFloor
            if (otherUser?.groupCount != thisUser?.groupCount) return false
            otherUser?.groups?.forEachIndexed { index, photoEntity ->
                if (thisUser?.groups?.get(index)?.id != photoEntity.id) return false
            }
            return (otherUser?.userTypeEnum == thisUser?.userTypeEnum)
        }

        UserProfileAdapterType.GIFTS_FLOOR -> {
            val otherUser = other as? UserEntityGiftsFloor
            val thisUser = this as? UserEntityGiftsFloor
            if (otherUser?.giftsNewCount != thisUser?.giftsNewCount) return false
            if (otherUser?.listGiftEntity?.size != thisUser?.listGiftEntity?.size) return false
            otherUser?.listGiftEntity?.forEachIndexed { index, photoEntity ->
                if (thisUser?.listGiftEntity?.get(index)?.giftId != photoEntity.giftId) return false
            }
            return (otherUser?.isMe == thisUser?.isMe
                    && otherUser?.accountTypeEnum == thisUser?.accountTypeEnum)
        }


        UserProfileAdapterType.ROAD_FLOOR -> {
            val otherUser = other as? UserEntityRoadFloor
            val thisUser = this as? UserEntityRoadFloor

            return (otherUser?.isMe == thisUser?.isMe
                    && otherUser?.postCount == thisUser?.postCount
                    && otherUser?.userTypeEnum == thisUser?.userTypeEnum)
        }

        UserProfileAdapterType.GARAGE_FLOOR -> {
            val otherUser = other as? UserEntityGarageFloor
            val thisUser = this as? UserEntityGarageFloor
            if (otherUser?.vehicleCount != thisUser?.vehicleCount) return false
            otherUser?.listVehicles?.forEachIndexed { index, vehicle ->
                if (thisUser?.listVehicles?.get(index)?.vehicleId != vehicle.vehicleId) return false
            }
            return (otherUser?.isMe == thisUser?.isMe
                    && otherUser?.accountTypeEnum == thisUser?.accountTypeEnum)
        }

        UserProfileAdapterType.BIRTHDAY_FLOOR -> {
            return true
        }
        UserProfileAdapterType.CLOSED_PROFILE_FLOOR -> {
            return true
        }
        UserProfileAdapterType.BLOCKED_PROFILE_FLOOR -> {
            return true
        }
        UserProfileAdapterType.BLOCKED_ME_PROFILE_FLOOR -> {
            return true
        }

        UserProfileAdapterType.MAP_FLOOR -> {
            val otherUser = other as? UserEntityMapFloor
            val thisUser = this as? UserEntityMapFloor
            return (otherUser?.isMe == thisUser?.isMe
                    && otherUser?.accountTypeEnum == thisUser?.accountTypeEnum
                    && otherUser?.countWhitelist == thisUser?.countWhitelist
                    && otherUser?.countBlacklist == thisUser?.countBlacklist
                    && otherUser?.value == thisUser?.value
                    && otherUser?.accountColor == thisUser?.accountColor
                    && otherUser?.distance == thisUser?.distance
                    && otherUser?.coordinates?.latitude == thisUser?.coordinates?.latitude
                    && otherUser?.coordinates?.longitude == thisUser?.coordinates?.longitude)
        }
        UserProfileAdapterType.HOLIDAY_FLOOR -> {
            return true
        }
        UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR -> {
            val otherEntity = other as? UserEntityFriendSubscribeFloor
            val thisEntity = this as? UserEntityFriendSubscribeFloor

            return (otherEntity?.userId == thisEntity?.userId
                    && otherEntity?.isUserBlacklisted == thisEntity?.isUserBlacklisted
                    && otherEntity?.isSubscribed == thisEntity?.isSubscribed
                    && otherEntity?.friendStatus == thisEntity?.friendStatus
                    && otherEntity?.userStatus == thisEntity?.userStatus
                    && otherEntity?.type == thisEntity?.type)
        }
        UserProfileAdapterType.DEFAULT_SKELETON_FLOOR -> {
            return true
        }
        UserProfileAdapterType.SUBSCRIBE_SKELETON_FLOOR -> {
            return true
        }
        UserProfileAdapterType.MUTUAL_SUBSCRIBERS_FLOOR -> {
            val otherEntity = other as? MutualSubscribersUiEntity
            val thisEntity = this as? MutualSubscribersUiEntity
            if (otherEntity?.mutualSubscribersFriends?.size != thisEntity?.mutualSubscribersFriends?.size) return false
            return otherEntity == thisEntity
        }
        UserProfileAdapterType.PROFILE_SUGGESTIONS -> {
            val otherEntity = other as? ProfileSuggestionsFloorUiEntity
            val thisEntity = this as? ProfileSuggestionsFloorUiEntity
            val areItemsSame = otherEntity == thisEntity
            return areItemsSame
        }
    }
}

fun UserProfileUIModel.toUserUpdateModel(): UserUpdateModel {
    return UserUpdateModel(
        uid = this.userId,
        name = this.name,
        uniqueName = this.uniquename,
        birthday = this.birthday.let(::Date),
        avatar = this.avatarDetails.avatarSmall,
        avatarBig = this.avatarDetails.avatarBig,
        gender = this.gender.let(Gender::fromValue),
        accountType = createAccountTypeEnum(this.accountDetails.accountType),
        accountColor = this.accountDetails.accountColor,
        city = this.locationDetails.cityName,
        country = this.locationDetails.countryName,
        approved = this.accountDetails.isAccountApproved,
        friendStatus = this.friendStatus.intStatus,
        subscriptionOn = this.settingsFlags.isSubscriptionOn,
        subscribersCount = this.subscribersCount,
        profileBlocked = this.accountDetails.isAccountBlocked,
        profileDeleted = this.accountDetails.isAccountDeleted,
        blacklistedByMe = this.settingsFlags.blacklistedByMe,
        blacklistedMe = this.settingsFlags.blacklistedMe
    )
}

fun UserProfileModel.toUserEntity(): UserEntity {
    return UserEntity(
        userId = userId,
        name = name,
        avatarBig = avatarBig,
        avatarSmall = avatarSmall,
        birthday = birthday,
        city = coordinates?.cityName.orEmpty(),
        color = accountColor ?: 0,
        gender = gender,
        status = profileStatus,
        type = accountType ?: 0,
        email = email,
        phone = phoneNumber,
        anonymousAvailable = isAnonym.toBoolean(),
        isBlockedByMe = profileBlocked.toBoolean(),
        uniqueName = uniquename,
        topContentMaker = topContentMaker
    )
}
