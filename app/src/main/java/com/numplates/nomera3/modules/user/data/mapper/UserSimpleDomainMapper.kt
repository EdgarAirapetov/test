package com.numplates.nomera3.modules.user.data.mapper

import com.meera.db.models.userprofile.UserSettingsFlags
import com.meera.db.models.userprofile.UserSimple
import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.LocationModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSettingsFlagsModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserVehicleModel
import javax.inject.Inject

class UserSimpleDomainMapper @Inject constructor() {
    fun mapToDomain(user: UserSimple): UserSimpleModel {
        return UserSimpleModel(
            userId = user.userId,
            name = user.name,
            birthday = user.birthday,
            avatarSmall = user.avatarSmall,
            gender = user.gender,
            accountType = user.accountType,
            accountColor = user.accountColor,
            cityName = user.city?.name,
            countryName = user.country?.name,
            profileDeleted = user.profileDeleted,
            profileBlocked = user.profileBlocked,
            profileVerified = user.profileVerified,
            groupType = user.groupType,
            blacklistedMe = user.blacklistedMe,
            blacklistedByMe = user.blacklistedByMe,
            uniqueName = user.uniqueName,
            isSystemAdministrator = user.isSystemAdministrator,
            approved = user.approved,
            topContentMaker = user.topContentMaker,
            mutualFriendsCount = user.mutualFriendsCount,
            coordinates = LocationModel(
                latitude = user.geo?.lat,
                longitude = user.geo?.lon,
                cityName = user.city?.name ?: "",
                countryName = user.country?.name ?: "",
                cityId = user.city?.id,
                countryId = user.country?.id
            ),
            settingsFlags = mapSettingsFlag(user.settingsFlags),
            mainVehicle = mapUserVehicle(user.mainVehicle),
            moments = user.moments,
            friendStatus = user.settingsFlags?.friendStatus ?: 0,
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

    private fun mapUserVehicle(vehicle: VehicleEntity?): UserVehicleModel? {
        vehicle ?: return null
        return UserVehicleModel(
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
    }
}
