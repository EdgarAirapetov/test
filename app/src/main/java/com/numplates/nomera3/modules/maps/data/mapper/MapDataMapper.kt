package com.numplates.nomera3.modules.maps.data.mapper

import android.location.Location
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.data.network.MapObjectsDto
import com.numplates.nomera3.data.network.MapUserDto
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.maps.data.model.UserCardDto
import com.numplates.nomera3.modules.maps.domain.model.MapClusterModel
import com.numplates.nomera3.modules.maps.domain.model.MapObjectsModel
import com.numplates.nomera3.modules.maps.domain.model.MapUserModel
import com.numplates.nomera3.modules.maps.domain.model.NearestFriendModel
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import java.util.Date
import javax.inject.Inject

class MapDataMapper @Inject constructor(
    private val holidayInfoHelper: HolidayInfoHelper
) {

    fun mapUserSnippets(userCards: List<UserCardDto>): List<UserSnippetModel> {
        return userCards.map { dto ->
            val birthdayDate = dto.birthday?.let(::Date)
            val coordinates = CoordinatesModel(
                lat = dto.coordinates.lat,
                lon = dto.coordinates.lon
            )
            val gender = dto.gender?.let(Gender::fromValue)
            val accountType = createAccountTypeEnum(dto.accountType)
            UserSnippetModel(
                uid = dto.uid,
                name = dto.name,
                uniqueName = dto.uniqueName,
                birthday = birthdayDate,
                avatar = dto.avatarSmall,
                avatarBig = dto.avatarBig,
                hatLink = holidayInfoHelper.getHatLink(accountType),
                gender = gender,
                distance = dto.distance,
                accountType = accountType,
                accountColor = dto.accountColor,
                city = dto.city?.name,
                country = dto.countryDto?.name,
                coordinates = coordinates,
                approved = dto.approved.toBoolean(),
                friendStatus = dto.friendStatus,
                subscriptionOn = dto.subscriptionOn.toBoolean(),
                subscribersCount = dto.subscribersCount,
                profileBlocked = dto.profileBlocked.toBoolean(),
                profileDeleted = dto.profileDeleted.toBoolean(),
                blacklistedByMe = dto.blacklistedByMe.toBoolean(),
                blacklistedMe = dto.blacklistedMe.toBoolean(),
                topContentMaker = dto.topContentMaker.toBoolean(),
                moments = dto.moments?.let(UserMomentsMapper::mapUserMomentsModel)
            )
        }
    }

    fun mapObjects(dto: MapObjectsDto): MapObjectsModel {
        val users = mapUsers(dto.users)
        val clusters = dto.clusters.map { mapClusterDto ->
            MapClusterModel(
                id = mapClusterDto.clusterId,
                size = mapClusterDto.size,
                gpsX = mapClusterDto.gpsX,
                gpsY = mapClusterDto.gpsY,
                capacity = mapClusterDto.capacity,
                users = mapUsers(mapClusterDto.users)
            )
        }
        val nearestFriend = dto.nearestFriend?.let {
            NearestFriendModel(
                id = it.id,
                location = CoordinatesModel(
                    lat = it.lat,
                    lon = it.lon
                )
            )
        }
        return MapObjectsModel(
            users = users,
            clusters = clusters,
            nearestFriend = nearestFriend
        )
    }

    fun mapCoordinates(location: Location): CoordinatesModel = CoordinatesModel(
        lat = location.latitude,
        lon = location.longitude
    )

    private fun mapUsers(users: List<MapUserDto>): List<MapUserModel> {
        return users.map { userPinDto ->
            MapUserModel(
                id = userPinDto.uid,
                accountType = createAccountTypeEnum(userPinDto.accountType),
                avatar = userPinDto.avatar,
                gender = Gender.fromValue(userPinDto.gender),
                accountColor = userPinDto.accountColor,
                coordinates = CoordinatesModel(
                    lat = userPinDto.coordinates?.lat ?: 0.0,
                    lon = userPinDto.coordinates?.lon ?: 0.0
                ),
                name = userPinDto.name,
                uniqueName = userPinDto.uniqueName,
                isFriend = userPinDto.isFriend.toBoolean(),
                blacklistedByMe = userPinDto.blacklistedByMe.toBoolean(),
                blacklistedMe = userPinDto.blacklistedMe.toBoolean(),
                moments = userPinDto.moments?.let(UserMomentsMapper::mapUserMomentsModel)
            )
        }
    }
}
