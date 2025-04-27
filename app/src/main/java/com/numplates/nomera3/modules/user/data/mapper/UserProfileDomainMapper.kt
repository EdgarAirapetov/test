package com.numplates.nomera3.modules.user.data.mapper

import com.meera.db.models.userprofile.GiftEntity
import com.meera.db.models.userprofile.GroupEntity
import com.meera.db.models.userprofile.MutualUsersEntity
import com.meera.db.models.userprofile.PhotoEntity
import com.meera.db.models.userprofile.ProductEntity
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.userprofile.UserSettingsFlags
import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.GiftModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.GroupModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.LocationModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.MutualUsersModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.PhotoModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.ProductHolidayModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSettingsFlagsModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserVehicleModel
import javax.inject.Inject

class UserProfileDomainMapper @Inject constructor(
    private val userSimpleMapper: UserSimpleDomainMapper
) {
    fun mapDataToDomain(profile: UserProfileNew): UserProfileModel {
        return UserProfileModel(
            userId = profile.userId,
            birthday = profile.birthday,
            birthdayFlag = profile.birthdayFlag,
            showBirthdayButton = profile.showBirthdayButton,
            avatarBig = profile.avatarBig,
            avatarSmall = profile.avatarSmall,
            accountColor = profile.accountColor,
            gender = profile.gender,
            name = profile.name,
            accountType = profile.accountType,
            profileRating = profile.profileRating,
            blacklistedByMe = profile.blacklistedByMe,
            blacklistedMe = profile.blacklistedMe,
            friendStatus = profile.friendStatus,
            friendsCount = profile.friendsCount,
            vehiclesCount = profile.vehiclesCount,
            groupsCount = profile.groupsCount,
            photosCount = profile.photosCount,
            postsCount = profile.postsCount,
            giftsCount = profile.giftsCount,
            giftsNewCount = profile.giftsNewCount,
            showOnMap = profile.showOnMap,
            canWriteAnonymousMessages = profile.canWriteAnonymousMessages,
            accountTypeExpiration = profile.accountTypeExpiration,
            profileDeleted = profile.profileDeleted,
            profileBlocked = profile.profileBlocked,
            profileVerified = profile.profileVerified,
            showBirthday = profile.showBirthday,
            mapState = profile.mapState,
            isAnonym = profile.isAnonym,
            membershipType = profile.membershipType,
            membershipStatus = profile.membershipStatus,
            hideBirthday = profile.hideBirthday,
            hideGender = profile.hideGender,
            subscriptionCount = profile.subscriptionCount,
            subscribersCount = profile.subscribersCount,
            friendsRequestCount = profile.friendsRequestCount,
            deletedAt = profile.deletedAt,
            uniquename = profile.uniquename,
            distance = profile.distance,
            isSystemAdmin = profile.isSystemAdmin,
            avatarAnimation = profile.avatarAnimation,
            complete = profile.complete,
            phoneNumber = profile.phoneNumber,
            email = profile.email,
            approved = profile.approved,
            topContentMaker = profile.topContentMaker,
            showFriendsAndSubscribers = profile.showFriendsAndSubscribers,
            profileStatus = profile.profileStatus,
            registrationDate = profile.registrationDate,
            mutualUsers = mapMutualUsers(profile.mutualUsersEntity),
            coordinates = LocationModel(
                latitude = profile.coordinates?.latitude,
                longitude = profile.coordinates?.longitude,
                cityName = profile.city?.name ?: "",
                countryName = profile.country?.name ?: "",
                cityId = profile.city?.id,
                countryId = profile.country?.id
            ),
            holidayProduct = mapProductModel(profile.holidayProduct),
            settingsFlags = mapSettingsFlag(profile.settingsFlags),
            vehicles = mapVehicles(profile.vehicles),
            gifts = mapGift(profile.gifts),
            photos = mapPhoto(profile.photos),
            groups = mapGroup(profile.groups),
            isProfileFilled = isProfileFilled(profile),
            role = profile.role,
            eventCount = profile.eventCount,
            moments = profile.moments?.let(UserMomentsMapper::mapUserMomentsModel)
        )
    }


    private fun isProfileFilled(profile: UserProfileNew): Boolean {
        return !profile.name.isNullOrEmpty()
            && profile.birthday != null
            && profile.country?.id != null
            && profile.country?.id != 0L
            && profile.city?.id != null
            && profile.city?.id != 0L
            && profile.complete == 1
    }


    private fun mapMutualUsers(mutualEntity: MutualUsersEntity?): MutualUsersModel? {
        mutualEntity ?: return null
        return MutualUsersModel(
            userIds = mutualEntity.userIds,
            moreCount = mutualEntity.moreCount,
            userSimple = mutualEntity.userSimple?.map { userSimple ->
                userSimpleMapper.mapToDomain(userSimple)
            }
        )
    }

    private fun mapGroup(groups: List<GroupEntity>?) = groups?.map { group ->
        GroupModel(
            groupId = group.groupId,
            avatar = group.avatar ?: "",
            name = group.name ?: "",
            countMembers = group.countMembers ?: 0
        )
    } ?: emptyList()

    private fun mapPhoto(photos: List<PhotoEntity>?) = photos?.map {
        PhotoModel(it.id, it.link, it.isAdult == 1)
    } ?: emptyList()

    private fun mapGift(models: List<GiftEntity>?) = models?.map { gift ->
        GiftModel(
            id = gift.giftId,
            typeId = gift.typeId,
            isReceived = gift.metadata?.isReceived ?: false,
            isViewed = gift.metadata?.isViewed ?: false,
            imageSmall = gift.imageSmall
        )
    } ?: emptyList()

    private fun mapVehicles(vehicles: List<VehicleEntity>?): List<UserVehicleModel> {
        return vehicles?.map { vehicle ->
            UserVehicleModel(
                vehicleId = vehicle.id,
                brandLogo = vehicle.brand?.logo,
                avatarSmall = vehicle.avatarSmall ?: "",
                hasNumber = vehicle.type?.hasNumber,
                brandName = vehicle.brand?.name,
                modelName = vehicle.model?.name,
                number = vehicle.number,
                typeId = vehicle.type?.typeId,
                countryId = vehicle.country?.countryId,
                isMain = vehicle.isMain
            )
        } ?: emptyList()
    }

    private fun mapProductModel(model: ProductEntity?): ProductHolidayModel? {
        model ?: return null
        return ProductHolidayModel(
            id = model.id,
            appleProductId = model.appleProductId,
            customTitle = model.customTitle,
            description = model.description,
            imageItem = ProductEntity.ImageItemEntity(
                link = model.imageItem.link,
                linkSmall = model.imageItem.linkSmall
            ),
            itunesProductId = model.itunesProductId,
            playMarketProductId = model.playMarketProductId,
            type = model.type,
            price = model.price,
            imageLink = model.imageItem.link,
            imageLinkSmall = model.imageItem.linkSmall
        )
    }

    private fun mapSettingsFlag(model: UserSettingsFlags?): UserSettingsFlagsModel {
        return UserSettingsFlagsModel(
            iCanCall = model?.iCanCall,
            isInCallBlacklist = model?.isInCallBlacklist,
            isInCallWhitelist = model?.isInCallWhitelist,
            userCanCallMe = model?.userCanCallMe,
            notificationsOff = model?.notificationsOff,
            subscription_on = model?.subscription_on,
            subscribedToMe = model?.subscribedToMe,
            subscription_notify = model?.subscription_notify,
            hideRoadPosts = model?.hideRoadPosts,
            friendStatus = model?.friendStatus,
            iCanChat = model?.iCanChat,
            userCanChatMe = model?.userCanChatMe,
            isInChatBlackList = model?.isInChatBlackList,
            isInChatWhiteList = model?.isInChatWhiteList,
            iCanGreet = model?.iCanGreet
        )
    }
}
