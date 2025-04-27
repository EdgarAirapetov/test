package com.numplates.nomera3.modules.maps.ui.friends.mapper

import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.utils.getAge
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.ui.friends.MapFriendsListsDelegate
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import java.util.Date
import javax.inject.Inject

class MapFriendUiMapper @Inject constructor(
    private val holidayInfoHelper: HolidayInfoHelper,
    private val networkStatusProvider: NetworkStatusProvider
) {

    fun mapUserSnippetModel(dto: MapFriendListItem.MapFriendUiModel): UserSnippetModel{
        val birthdayDate = dto.birthday.let(::Date)
        val coordinates = CoordinatesModel(
            lat = dto.location.latitude,
            lon = dto.location.longitude
        )
        val gender = dto.gender?.let(Gender::fromValue)
        val accountType = createAccountTypeEnum(dto.accountType)
        return UserSnippetModel(
            uid = dto.userId,
            name = dto.name,
            uniqueName = dto.uniqueName,
            birthday = birthdayDate,
            avatar = dto.avatarUrl,
            avatarBig = dto.avatarUrl,
            hatLink = holidayInfoHelper.getHatLink(accountType),
            gender = gender,
            distance = 0.0,
            accountType = accountType,
            accountColor = dto.accountColor,
            city = dto.city,
            country = dto.countryDto,
            coordinates = coordinates,
            approved = dto.approved.toBoolean(),
            friendStatus = dto.friendStatus,
            subscriptionOn = dto.subscriptionOn.toBoolean(),
            subscribersCount = 0,
            profileBlocked = dto.profileBlocked,
            profileDeleted = dto.profileDeleted,
            blacklistedByMe = dto.blacklistedByMe,
            blacklistedMe = dto.blacklistedMe,
            topContentMaker = dto.topContentMaker,
            moments = dto.moments
        )
    }
    fun mapUserUiModel(user: UserProfileModel): UserSimpleModel {
        return UserSimpleModel(
            userId = user.userId,
            name = user.name,
            birthday = user.birthday,
            avatarSmall = user.avatarSmall,
            gender = user.gender,
            accountType = user.accountType,
            accountColor = user.accountColor,
            cityName = "",
            countryName = "",
            profileDeleted = user.profileDeleted,
            profileBlocked = user.profileBlocked,
            profileVerified = user.profileVerified,
            groupType = null,
            blacklistedMe = user.blacklistedMe,
            blacklistedByMe = user.blacklistedByMe,
            uniqueName = user.uniquename,
            isSystemAdministrator = user.isSystemAdmin,
            approved = user.approved,
            topContentMaker = user.topContentMaker,
            mutualFriendsCount = null,
            coordinates = user.coordinates!!,
            settingsFlags = user.settingsFlags,
            mainVehicle = null,
            moments = UserMomentsDto(
                hasMoments = user.moments?.hasMoments.toInt(),
                hasNewMoments = user.moments?.hasNewMoments.toInt(),
                countNew = user.moments?.countNew,
                countTotal = user.moments?.countTotal,
                previews = null
                ),
            friendStatus = user.settingsFlags?.friendStatus ?: 0,
        )
    }

    fun mapUserUiModel(userModel: MapFriendListItem.MapFriendUiModel): MapUserUiModel {
        val accountType = createAccountTypeEnum(userModel.accountType)
        return MapUserUiModel(
            id = userModel.userId,
            accountType = accountType,
            avatar = userModel.avatarUrl,
            hatLink = holidayInfoHelper.getHatLink(accountType),
            gender = Gender.FEMALE,
            accountColor = userModel.accountColor,
            latLng = userModel.location,
            name = userModel.name,
            uniqueName = userModel.uniqueName,
            isFriend = userModel.friendStatus == FRIEND_STATUS_CONFIRMED,
            blacklistedByMe = userModel.blacklistedByMe,
            blacklistedMe = userModel.blacklistedMe,
            hasMoments = userModel.moments?.hasMoments.isTrue(),
            hasNewMoments = userModel.moments?.hasNewMoments.isTrue(),
            moments = userModel.moments
        )
    }

    fun mapFriendsListItems(
        participantUsers: List<UserSimpleModel>,
        pagingData: MapFriendsListsDelegate.PagingDataUiModel,
    ): MapFriendsListUiModel {

        val result = mapUiModel(participantUsers, pagingData.isLoadingNextPage, pagingData.isLastPage, pagingData.updatePosition).items
        val items = when {
            !networkStatusProvider.isInternetConnected() -> {
                result.plus(getLoadingStubs(participantUsers.isEmpty()))
            }
            pagingData.isLoadingNextPage || pagingData.firstStart == true -> {
                result.plus(getLoadingStubs(participantUsers.isEmpty()))
            }
            result.isEmpty() && !pagingData.search.isNullOrEmpty() -> {
                result.plus(MapFriendListItem.EmptyItemUiModel)
            }
            result.isEmpty() && pagingData.search.isNullOrEmpty() && !pagingData.isLoadingNextPage -> {
                listOf(MapFriendListItem.FindFriendItemUiModel)
            }
            else -> result.plus(getEmptyItemsSearch())
        }
        return MapFriendsListUiModel(
            items = items,
            isLoadingNextPage = pagingData.isLoadingNextPage,
            isLastPage = pagingData.isLastPage,
            updatePosition = pagingData.updatePosition
        )
    }

    private fun mapUiModel(
        participantUsers: List<UserSimpleModel>,
        isLoadingNextPage: Boolean,
        isLastPage: Boolean,
        updatePosition: Boolean
    ): MapFriendsListUiModel {
        return MapFriendsListUiModel(
            items = mapEventParticipantListItems(
                participantUsers = participantUsers,
            ),
            isLoadingNextPage = isLoadingNextPage,
            isLastPage = isLastPage,
            updatePosition = updatePosition
        )
    }

    private fun getEmptyItemsSearch(): List<MapFriendListItem.EmptySearchItemUiModel> =
        listOf(
            MapFriendListItem.EmptySearchItemUiModel,
            MapFriendListItem.EmptySearchItemUiModel,
            MapFriendListItem.EmptySearchItemUiModel,
        )

    private fun getLoadingStubs(isInitial: Boolean): List<MapFriendListItem.StubItemUiModel> =
        listOf(
            MapFriendListItem.StubItemUiModel(isInitial = isInitial, position = 0),
            MapFriendListItem.StubItemUiModel(isInitial = isInitial, position = 1),
            MapFriendListItem.StubItemUiModel(isInitial = isInitial, position = 2),
            MapFriendListItem.StubItemUiModel(isInitial = isInitial, position = 3),
        )

    private fun mapEventParticipantListItems(
        participantUsers: List<UserSimpleModel>
    ): List<MapFriendListItem.MapFriendUiModel> {
        return participantUsers.map { user ->
            val age = user.birthday?.let(::getAge)?.let { "$it, " } ?: ""
            val ageLocation = "$age${user.cityName.orEmpty()}"
            MapFriendListItem.MapFriendUiModel(
                userId = user.userId,
                name = user.name.orEmpty(),
                uniqueName = "@${user.uniqueName.orEmpty()}",
                ageLocation = ageLocation,
                avatarUrl = user.avatarSmall.orEmpty(),
                accountType = user.accountType ?: 0,
                profileBlocked = user.profileBlocked.toBoolean(),
                profileDeleted = user.profileDeleted.toBoolean(),
                blacklistedByMe = user.blacklistedByMe.toBoolean(),
                blacklistedMe = user.blacklistedMe.toBoolean(),
                approved = user.approved,
                topContentMaker = user.topContentMaker.toBoolean(),
                moments = user.moments?.let(UserMomentsMapper::mapUserMomentsModel),
                location = LatLng(user.coordinates.latitude ?: 0.0, user.coordinates.longitude ?: 0.0),
                accountColor = user.accountColor ?: 0,
                friendStatus = user.settingsFlags?.friendStatus ?: 0,
                birthday = user.birthday ?: 0L,
                city = user.cityName.orEmpty(),
                countryDto = user.countryName.orEmpty(),
                gender = user.gender ?: 0,
                subscribersCount = 0,
                subscriptionOn = user.settingsFlags?.subscription_on ?: 0,
                iCanChat = user.settingsFlags?.iCanChat.toBoolean()
            )
        }
    }
}
