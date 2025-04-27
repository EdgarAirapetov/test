package com.numplates.nomera3.modules.services.ui.mapper

import com.meera.core.extensions.toBoolean
import com.meera.core.utils.getAge
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesButtonsUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesPlaceholderUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecentUsersUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecommendedPeopleUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel
import com.numplates.nomera3.modules.services.ui.entity.ServicesCommunityUiModel
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import javax.inject.Inject

class MeeraServicesContentMapper @Inject constructor() {

    fun mapContent(
        userUiModel: MeeraServicesUserUiModel?,
        recents: List<RecentUserUiModel>,
        recommendedPeople: List<PeopleRelatedUserModel>,
        communities: MeeraServicesCommunitiesUiModel,
    ): List<MeeraServicesUiModel> {
        val list = mutableListOf<MeeraServicesUiModel?>()
        list.add(userUiModel)
        list.add(MeeraServicesButtonsUiModel)
        if (recents.isNotEmpty()) {
            list.add(MeeraServicesRecentUsersUiModel(recents))
        }
        if (recommendedPeople.isNotEmpty()) {
            list.add(MeeraServicesRecommendedPeopleUiModel(recommendedPeople.toRelatedUsersUiEntity()))
        }
        if (communities.communities.isNotEmpty()) {
            list.add(communities)
        } else {
            list.add(MeeraServicesCommunitiesPlaceholderUiModel)
        }
        return list.filterNotNull()
    }

    fun mapPaginationRecommendedListToUiList(
        currentList: List<MeeraServicesUiModel>,
        newList: List<PeopleRelatedUserModel>,
        rootListPosition: Int
    ): List<MeeraServicesUiModel> {
        val listResult = currentList.toMutableList()
        val newMappedList = newList.toRelatedUsersUiEntity()
        if (rootListPosition >= listResult.size) return listResult
        val currentInnerList =
            (listResult[rootListPosition] as? MeeraServicesRecommendedPeopleUiModel)?.users?.toMutableList()
                ?: mutableListOf()
        currentInnerList.addAll(newMappedList)
        val newEntity = (currentList[rootListPosition] as? MeeraServicesRecommendedPeopleUiModel)?.copy(
            users = currentInnerList
        ) ?: return listResult
        listResult[rootListPosition] = newEntity
        return listResult
    }

    fun mapPaginationCommunitiesListToUiList(
        currentList: List<MeeraServicesUiModel>,
        newList: List<ServicesCommunityUiModel>,
        rootListPosition: Int
    ): List<MeeraServicesUiModel> {
        val listResult = currentList.toMutableList()
        if (rootListPosition >= listResult.size) return listResult
        val currentInnerList =
            (listResult[rootListPosition] as? MeeraServicesCommunitiesUiModel)?.communities?.toMutableList()
                ?: mutableListOf()
        newList.forEach { newItem ->
            if (currentInnerList.none { it == newItem }) currentInnerList.add(newItem)
        }
        val newEntity = (currentList[rootListPosition] as? MeeraServicesCommunitiesUiModel)?.copy(
            communities = currentInnerList
        ) ?: return listResult
        listResult[rootListPosition] = newEntity
        return listResult
    }

    fun getUpdatedUserFriendStatusListById(
        currentList: List<MeeraServicesUiModel>,
        selectedUserId: Long,
        isUserSubscribed: Boolean,
    ): List<MeeraServicesUiModel> {
        if (currentList.isEmpty()) return emptyList()
        val recommendations = currentList
            .filterIsInstance<MeeraServicesRecommendedPeopleUiModel>()
            .firstOrNull()
        val currentRelatedUsers = recommendations?.users ?: emptyList()
        if (currentRelatedUsers.isEmpty()) return currentList
        val newRelated = ArrayList(currentRelatedUsers.map { model ->
            if (model.userId == selectedUserId) {
                model.copy(hasFriendRequest = isUserSubscribed)
            } else {
                model
            }
        })
        val newRelatedList = MeeraServicesRecommendedPeopleUiModel(newRelated)
        val listResult = currentList.toMutableList().map { uiEntity ->
            if (uiEntity is MeeraServicesRecommendedPeopleUiModel) {
                newRelatedList
            } else {
                uiEntity
            }
        }
        return listResult
    }

    private fun List<PeopleRelatedUserModel>.toRelatedUsersUiEntity(): List<RecommendedPeopleUiEntity> =
        this.map { user ->
            RecommendedPeopleUiEntity(
                userId = user.userId,
                userAvatarUrl = user.avatar,
                userName = user.name,
                userAge = user.birthday,
                userCity = user.cityName,
                accountColor = user.accountColor,
                accountType = user.accountType,
                fullUserAgeCity = "${getAgeStr(user.birthday)}, ${user.cityName}",
                mutualUsersEntity = MutualFriendsUiEntity(
                    mutualFriends = user.mutualFriends?.mutualFriends ?: emptyList(),
                    accountTypeEnum = AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                    moreCount = 0
                ),
                totalMutualUsersCount = user.mutualTotalCount,
                subscriptionOn = user.settingsFlags?.subscription_on ?: 0,
                isSubscribedToMe = user.settingsFlags?.subscribedToMe.toBoolean(),
                isAllowToShowAge = user.birthday != null,
                accountTypeEnum = createAccountTypeEnum(user.accountType),
                friendStatus = user.settingsFlags?.friendStatus ?: 0,
                isAccountApproved = user.approved.toBoolean(),
                hasFriendRequest = user.hasFriendRequest,
                topContentMaker = user.topContentMaker
            )
        }

    private fun getAgeStr(birthday: Long?): String {
        return getAge(birthday ?: 0)
    }

}
