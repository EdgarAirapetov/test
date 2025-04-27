package com.numplates.nomera3.modules.search.domain.mapper.result

import com.meera.core.extensions.toBoolean
import com.meera.core.utils.getAge
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import javax.inject.Inject

class SearchUserResultMapper @Inject constructor() {

    fun mapToPeoplesUiEntity(simpleUsers: List<UserSimple>, myUserId: Long): List<UserSearchResultUiEntity> {
        return simpleUsers.map { simpleUser ->
            val buttonStatus = if (myUserId == simpleUser.userId) {
                UserSearchResultUiEntity.ButtonState.Hide
            } else {
                FriendStatusButtonMapper().mapForPeoples(simpleUser.settingsFlags?.friendStatus ?: -1)
            }
            val age = simpleUser.birthday?.let { birth ->
                getAge(birth)
            }

            val city = simpleUser.city?.name

            val additionalInfo = if (age.isNullOrEmpty().not() && city.isNullOrEmpty().not()) {
                "$age, $city"
            } else if (age.isNullOrEmpty() && city.isNullOrEmpty().not()) {
                city
            } else {
                null
            }

            return@map UserSearchResultUiEntity(
                uid = simpleUser.userId,
                name = simpleUser.name,
                avatarImage = simpleUser.avatarSmall,
                tagName = simpleUser.uniqueName,
                additionalInfo = additionalInfo,
                vehicle = simpleUser.mainVehicle,
                gender = simpleUser.gender,
                accountType = createAccountTypeEnum(simpleUser.accountType),
                accountColor = simpleUser.accountColor,
                friendStatus = simpleUser.settingsFlags?.friendStatus ?: 0,
                isSubscribed = (simpleUser.settingsFlags?.subscription_on ?: 0) == 1,
                isBlackListedByMe = simpleUser.blacklistedByMe == 1,
                buttonState = buttonStatus,
                approved = simpleUser.approved,
                topContentMaker = simpleUser.topContentMaker,
                isMyProfile = myUserId == simpleUser.userId,
                hasMoments = simpleUser.moments?.hasMoments.toBoolean(),
                hasNewMoments = simpleUser.moments?.hasNewMoments.toBoolean()
            )
        }
    }

    fun mapToSearchItem(simpleUsers: List<UserSimple>, myUserId: Long): List<SearchItem> {
        return simpleUsers.map { simpleUser ->
            val buttonStatus = if (myUserId == simpleUser.userId) {
                SearchItem.User.ButtonState.Hide
            } else {
                FriendStatusButtonMapper().map(simpleUser.settingsFlags?.friendStatus ?: -1)
            }
            val age = simpleUser.birthday?.let { birth ->
                getAge(birth)
            }

            val city = simpleUser.city?.name

            val additionalInfo = if (age.isNullOrEmpty().not() && city.isNullOrEmpty().not()) {
                "$age, $city"
            } else if (age.isNullOrEmpty() && city.isNullOrEmpty().not()) {
                city
            } else {
                null
            }

            return@map SearchItem.User(
                uid = simpleUser.userId,
                name = simpleUser.name,
                avatarImage = simpleUser.avatarSmall,
                tagName = simpleUser.uniqueName,
                additionalInfo = additionalInfo,
                vehicle = simpleUser.mainVehicle,
                gender = simpleUser.gender,
                accountType = createAccountTypeEnum(simpleUser.accountType),
                accountColor = simpleUser.accountColor,
                friendStatus = simpleUser.settingsFlags?.friendStatus ?: 0,
                isSubscribed = (simpleUser.settingsFlags?.subscription_on ?: 0) == 1,
                isBlackListedByMe = simpleUser.blacklistedByMe == 1,
                buttonState = buttonStatus,
                approved = simpleUser.approved,
                topContentMaker = simpleUser.topContentMaker,
                isMyProfile = myUserId == simpleUser.userId,
                hasMoments = simpleUser.moments?.hasMoments.toBoolean(),
                hasNewMoments = simpleUser.moments?.hasNewMoments.toBoolean()
            )
        }
    }
}
