package com.numplates.nomera3.modules.userprofile.data.mapper

import com.meera.db.models.userprofile.AvatarModel
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Coordinates
import com.meera.db.models.userprofile.Country
import com.meera.db.models.userprofile.GiftEntity
import com.meera.db.models.userprofile.GiftSenderUser
import com.meera.db.models.userprofile.GroupEntity
import com.meera.db.models.userprofile.Metadata
import com.meera.db.models.userprofile.PhotoEntity
import com.meera.db.models.userprofile.ProductEntity
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.userprofile.UserSettingsFlags
import com.meera.db.models.userprofile.UserSimple
import com.meera.db.models.userprofile.VehicleBrand
import com.meera.db.models.userprofile.VehicleCountry
import com.meera.db.models.userprofile.VehicleEntity
import com.meera.db.models.userprofile.VehicleModel
import com.meera.db.models.userprofile.VehicleType
import com.numplates.nomera3.modules.baseCore.data.model.CityDto
import com.numplates.nomera3.modules.baseCore.data.model.CountryDto
import com.numplates.nomera3.modules.userprofile.data.entity.GiftDto
import com.numplates.nomera3.modules.userprofile.data.entity.GiftSenderUserDto
import com.numplates.nomera3.modules.userprofile.data.entity.GroupDto
import com.numplates.nomera3.modules.userprofile.data.entity.PhotoDto
import com.numplates.nomera3.modules.userprofile.data.entity.ProductDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserProfileDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserSettingsFlagsDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto
import com.numplates.nomera3.modules.userprofile.data.entity.VehicleBrandDto
import com.numplates.nomera3.modules.userprofile.data.entity.VehicleCountryDto
import com.numplates.nomera3.modules.userprofile.data.entity.VehicleDto
import com.numplates.nomera3.modules.userprofile.data.entity.VehicleModelDto
import com.numplates.nomera3.modules.userprofile.data.entity.VehicleTypeDto
import javax.inject.Inject

class UserProfileDtoToDbMapper @Inject constructor() {
    fun mapDtoToDb(user: UserProfileDto): UserProfileNew {
        return UserProfileNew(
            userId = user.userId,
            birthday = user.birthday,
            birthdayFlag = user.birthdayFlag,
            avatarBig = user.avatarBig,
            showBirthdayButton = user.showBirthdayButton,
            avatarSmall = user.avatarSmall,
            accountColor = user.accountColor,
            gender = user.gender,
            name = user.name,
            accountType = user.accountType,
            city = mapCity(user.city),
            country = mapCountry(user.country),
            profileRating = user.profileRating,
            blacklistedByMe = user.blacklistedByMe,
            blacklistedMe = user.blacklistedMe,
            friendStatus = user.friendStatus,
            friendsCount = user.friendsCount,
            vehiclesCount = user.vehiclesCount,
            groupsCount = user.groupsCount,
            photosCount = user.photosCount,
            postsCount = user.postsCount,
            vehicles = mapVehicles(user.vehicles),
            gifts = mapGifts(user.gifts),
            giftsCount = user.giftsCount,
            giftsNewCount = user.giftsNewCount,
            photos = mapPhotos(user.photos),
            showOnMap = user.showOnMap,
            canWriteAnonymousMessages = user.canWriteAnonymousMessages,
            groups = mapGroups(user.groups),
            accountTypeExpiration = user.accountTypeExpiration,
            profileDeleted = user.profileDeleted,
            profileBlocked = user.profileBlocked,
            profileVerified = user.profileVerified,
            mainVehicle = mapVehicle(user.mainVehicle),
            showBirthday = user.showBirthday,
            coordinates = if (user.coordinates == null) null
            else Coordinates(user.coordinates.lat, user.coordinates.lon),
            mapState = user.mapState,
            isAnonym = user.isAnonym,
            membershipType = user.membershipType,
            membershipStatus = user.membershipStatus,
            hideBirthday = user.hideBirthday,
            hideGender = user.hideGender,
            settingsFlags = mapSettingsFlags(user.settingsFlags),
            subscriptionCount = user.subscriptionCount,
            subscribersCount = user.subscribersCount,
            friendsRequestCount = user.friendsRequestCount,
            deletedAt = user.deletedAt,
            uniquename = user.uniquename,
            distance = user.distance,
            isSystemAdmin = user.isSystemAdmin,
            holidayProduct = mapHolidayProduct(user.holidayProduct),
            avatarAnimation = user.avatarAnimation,
            complete = user.complete,
            phoneNumber = user.phoneNumber,
            email = user.email,
            approved = user.approved,
            topContentMaker = user.topContentMaker,
            showFriendsAndSubscribers = user.showFriendsAndSubscribers,
            mutualUsersEntity = user.mutualUsersEntity,
            profileStatus = user.profileStatus,
            registrationDate = user.registrationDate,
            inviterId = user.inviterId,
            countryByNumber = user.countryByNumber,
            moments = user.moments
        )
    }

    private fun mapVehicles(vehicles: List<VehicleDto>?): List<VehicleEntity>? {
        vehicles ?: return null
        return vehicles.mapNotNull { vehicle ->
            mapVehicle(vehicle)
        }
    }

    private fun mapVehicle(vehicle: VehicleDto?): VehicleEntity? {
        vehicle ?: return null
        return VehicleEntity(
            id = vehicle.id,
            number = vehicle.number ?: "",
            image = vehicle.image,
            avatarBig = vehicle.avatarBig,
            avatarSmall = vehicle.avatarSmall,
            isMain = vehicle.isMain,
            description = vehicle.description,
            brandIcon = vehicle.brandIcon,
            type = mapVehicleType(vehicle.type),
            brand = mapBrand(vehicle.brand),
            model = mapModel(vehicle.model),
            country = mapVehicleCountry(vehicle.country)
        )
    }

    private fun mapVehicleCountry(country: VehicleCountryDto?): VehicleCountry? {
        country ?: return null
        return VehicleCountry(country.countryId, country.name, country.flag)
    }

    private fun mapModel(model: VehicleModelDto?): VehicleModel? {
        model ?: return null
        return VehicleModel(model.modelId, model.name, model.brandId)
    }

    private fun mapBrand(brand: VehicleBrandDto?): VehicleBrand? {
        brand ?: return null
        return VehicleBrand(
            brandId = brand.brandId,
            name = brand.name,
            logo = brand.logo.orEmpty()
        )
    }

    private fun mapVehicleType(type: VehicleTypeDto?): VehicleType? {
        type ?: return null
        return VehicleType(
            typeId = type.typeId,
            name = type.name,
            hasNumber = type.hasNumber,
            hasBrands = type.hasBrands,
            hasModels = type.hasModels,
            icon = type.icon.orEmpty()
        )
    }

    fun mapToUserSimple(user: UserSimpleDto) = UserSimple(
        userId = user.userId,
        name = user.name,
        birthday = user.birthday,
        avatarSmall = user.avatarSmall,
        gender = user.gender,
        accountType = user.accountType,
        accountColor = user.accountColor,
        city = mapCity(user.city),
        country = mapCountry(user.country),
        mainVehicle = null,
        profileDeleted = user.profileDeleted,
        profileBlocked = user.profileBlocked,
        profileVerified = user.profileVerified,
        groupType = user.groupType,
        blacklistedMe = user.blacklistedMe,
        blacklistedByMe = user.blacklistedByMe,
        settingsFlags = mapSettingsFlags(user.settingsFlags),
        uniqueName = user.uniqueName,
        isSystemAdministrator = user.isSystemAdministrator,
        approved = user.approved,
        topContentMaker = user.topContentMaker,
        mutualFriendsCount = user.mutualFriendsCount,
        geo = user.geo,
        moments = user.moments
    )


    private fun mapHolidayProduct(holidayProduct: ProductDto?): ProductEntity? {
        holidayProduct ?: return null
        return ProductEntity(
            id = holidayProduct.id,
            appleProductId = holidayProduct.appleProductId,
            customTitle = holidayProduct.customTitle,
            description = holidayProduct.description,
            imageItem = ProductEntity.ImageItemEntity(
                holidayProduct.imageItem.link,
                holidayProduct.imageItem.linkSmall
            ),
            itunesProductId = holidayProduct.itunesProductId,
            playMarketProductId = holidayProduct.playMarketProductId,
            type = holidayProduct.type,
            price = holidayProduct.price.orEmpty()
        )
    }

    private fun mapSettingsFlags(settingsFlags: UserSettingsFlagsDto?): UserSettingsFlags? {
        settingsFlags ?: return null
        return UserSettingsFlags(
            iCanCall = settingsFlags.iCanCall,
            isInCallBlacklist = settingsFlags.isInCallBlacklist,
            isInCallWhitelist = settingsFlags.isInCallWhitelist,
            userCanCallMe = settingsFlags.userCanCallMe,
            notificationsOff = settingsFlags.notificationsOff,
            subscription_on = settingsFlags.subscription_on,
            subscribedToMe = settingsFlags.subscribedToMe,
            subscription_notify = settingsFlags.subscription_notify,
            hideRoadPosts = settingsFlags.hideRoadPosts,
            friendStatus = settingsFlags.friendStatus,
            iCanChat = settingsFlags.iCanChat,
            userCanChatMe = settingsFlags.userCanChatMe,
            isInChatBlackList = settingsFlags.isInChatBlackList,
            isInChatWhiteList = settingsFlags.isInChatWhiteList,
            iCanGreet = settingsFlags.iCanGreet
        )
    }


    private fun mapGroups(groups: List<GroupDto>?): List<GroupEntity>? {
        groups ?: return null
        return groups.map { groupDto ->
            GroupEntity(
                groupDto.groupId,
                groupDto.avatar,
                groupDto.name,
                groupDto.countMembers
            )
        }
    }

    private fun mapPhotos(photos: List<PhotoDto>?): List<PhotoEntity>? {
        photos ?: return null
        return photos.map { photoDto -> PhotoEntity(photoDto.id, photoDto.link, photoDto.isAdult) }
    }

    private fun mapGifts(gifts: List<GiftDto>?): List<GiftEntity>? {
        gifts ?: return null
        return gifts.map { giftDto ->
            GiftEntity(
                giftId = giftDto.giftId,
                imageBig = giftDto.imageBig,
                imageSmall = giftDto.imageSmall,
                typeCode = giftDto.typeCode,
                addedAt = giftDto.addedAt,
                comment = giftDto.comment,
                playMarketProductId = giftDto.playMarketProductId,
                appleProductId = giftDto.appleProductId,
                customTitle = giftDto.customTitle,
                holidayTitle = giftDto.holidayTitle,
                iTunesProductId = giftDto.iTunesProductId,
                senderUser = getGiftSenderUser(giftDto.senderUser),
                metadata = Metadata(
                    coffeeCode = giftDto.metadata?.coffeeCode,
                    coffeeType = giftDto.metadata?.coffeeType,
                    isViewed = giftDto.metadata?.isViewed ?: false,
                    isReceived = giftDto.metadata?.isReceived ?: false,
                ),
                typeId = giftDto.typeId
            )
        }
    }

    private fun getGiftSenderUser(user: GiftSenderUserDto?): GiftSenderUser? {
        user ?: return null
        return GiftSenderUser(
            userId = user.userId,
            birthday = user.birthday,
            accountColor = user.accountColor,
            accountType = user.accountType,
            avatar = if (user.avatar == null) null else AvatarModel(user.avatar?.avatarBig, user.avatar?.avatarSmall),
            city = user.city,
            gender = user.gender,
            name = user.name,
            uniqueName = user.uniqueName
        )
    }

    private fun mapCountry(country: CountryDto?): Country? {
        country ?: return null
        return Country(country.id.toLong(), country.name)
    }

    private fun mapCity(city: CityDto?): City? {
        city ?: return null
        return City(city.id.toLong(), city.name)
    }
}
