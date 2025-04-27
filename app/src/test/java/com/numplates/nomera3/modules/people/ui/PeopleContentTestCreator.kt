package com.numplates.nomera3.modules.people.ui

import androidx.annotation.DrawableRes
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FindPeoplesUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FriendFindContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity


private const val MIN_SUBSCRIBERS_COUNT = 999

private const val MIN_MEDIA_CONTENT_LIST_SIZE = 3
private const val SHIMMER_ITEM_COUNT = 3

class PeopleContentTestCreator {

    fun createDefaultContent(): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(createHeaderEntity("Добавить друзей"))
        listResult.addAll(getFindFriendsContent())
        listResult.addAll(getShimmerContent())
        return listResult
    }

    fun createContent(): List<PeoplesContentUiEntity> {
        return createFakePeopleContent(
            peopleApprovedUserModels = createFakePeopleApprovedUsers(),
            peopleRelatedUserModels = createFakeRelatedUsers(),
            myUserId = 0
        )
    }

    fun createFakePeopleApprovedUsers(): List<PeopleApprovedUserModel> {
        return listOf(
            PeopleApprovedUserModel(
                userId = 0,
                subscribersCount = 0,
                userName = String.empty(),
                accountType = 0,
                approved = 0,
                accountColor = 0,
                topContentMaker = 0,
                avatarSmall = String.empty(),
                uniqueName = String.empty(),
                settingsFlags = null,
                isUserSubscribed = false,
                posts = listOf()
            )
        )
    }

    fun createFakeRelatedUsers(): List<PeopleRelatedUserModel> {
        return listOf(
            PeopleRelatedUserModel(
                userId = 0,
                name = String.empty(),
                accountColor = 0,
                accountType = 0,
                approved = 0,
                avatar = String.empty(),
                birthday = 0,
                cityId = 0,
                cityName = String.empty(),
                countryId = 0,
                countryName = String.empty(),
                gender = 0,
                settingsFlags = null,
                mutualFriends = null,
                mutualTotalCount = 0,
                hasFriendRequest = false,
                topContentMaker = false
            )
        )
    }

    private fun getShimmerContent(): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(
            createHeaderEntity(
                text = "Рекомендации",
                drawableTextRes = R.drawable.ic_info_grey_people
            )
        )
        repeat(SHIMMER_ITEM_COUNT) {
            listResult.add(PeoplesShimmerUiEntity)
        }
        return listResult
    }

    private fun createFakePeopleContent(
        peopleApprovedUserModels: List<PeopleApprovedUserModel>,
        peopleRelatedUserModels: List<PeopleRelatedUserModel>,
        myUserId: Long,
    ): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(createHeaderEntity("Добавить друзей"))
        if (peopleRelatedUserModels.isNotEmpty()) {
            val mappedRelatedUsers = mapRelatedUsers(peopleRelatedUserModels)
            listResult.addAll(mappedRelatedUsers)
        }
        listResult.addAll(getFindFriendsContent())
        if (peopleApprovedUserModels.isNotEmpty()) {
            listResult.add(
                createHeaderEntity(
                    text = "Рекомендации",
                    drawableTextRes = R.drawable.ic_info_grey_people
                )
            )
            val mappedTopUsers = mapFromApprovedUsers(
                approvedUsers = peopleApprovedUserModels,
                myUserId = myUserId
            )
            listResult.addAll(mappedTopUsers)
        }
        return listResult
    }

    private fun getFindFriendsContent(): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(
            FindPeoplesUiEntity(
                label = "Поиск по имени",
                description = "Найди друзей по всему миру",
                icon = R.drawable.ic_search_ui_purple,
                contentType = FriendFindContentType.FIND_FRIENDS
            )
        )
        listResult.add(
            FindPeoplesUiEntity(
                label = "Шейк",
                description = "Потряси телефон, чтобы добавить друзей рядом",
                icon = R.drawable.ic_bump_ui_purple,
                contentType = FriendFindContentType.BUMP,
            )
        )
        listResult.add(
            FindPeoplesUiEntity(
                label = "Позвать друзей",
                description = "Пригласи друзей и получи VIP-статус",
                icon = R.drawable.ic_invite_friends_ui_purple,
                contentType = FriendFindContentType.INVITE_FRIENDS,
                isNeedToDrawSeparator = false
            )
        )
        return listResult
    }

    private fun mapFromApprovedUsers(
        approvedUsers: List<PeopleApprovedUserModel>,
        myUserId: Long
    ): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        approvedUsers.forEach loop@ { user ->
            if (user.posts.size <= MIN_MEDIA_CONTENT_LIST_SIZE) return@loop

            val approvedUser = createApprovedUser(
                user = user,
                myUserId = myUserId
            )
            listResult.add(approvedUser)
            listResult.add(
                BloggerMediaContentListUiEntity(
                    userId = 0,
                    bloggerPostList = listOf(
                        BloggerMediaContentUiEntity.BloggerImageContentUiEntity(
                            imageUrl = String.empty(),
                            rootUser = approvedUser,
                            postId = 0
                        )
                    )
                )
            )
        }
        return listResult
    }

    private fun createApprovedUser(
        user: PeopleApprovedUserModel,
        myUserId: Long
    ): PeopleInfoUiEntity {
        val hasMoreThanThousandSubscribers = user.subscribersCount > MIN_SUBSCRIBERS_COUNT
        return PeopleInfoUiEntity(
            userId = user.userId,
            userName = user.userName,
            accountType = createAccountTypeEnum(user.accountType),
            userSubscribers = user.subscribersCount,
            isApprovedAccount = user.approved.toBoolean(),
            isInterestingAuthor = user.topContentMaker.toBoolean(),
            imageUrl = user.avatarSmall,
            isUserSubscribed = user.isUserSubscribed,
            isUserSubscribedToMe = user.settingsFlags?.subscribedToMe.toBoolean(),
            hasMoreThanThousandSubscribers = hasMoreThanThousandSubscribers,
            isMe = user.userId == myUserId,
            uniqueName = user.uniqueName,
            hasMoments = false,
            hasNewMoments = false,
        )
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
                fullUserAgeCity = String.empty(),
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

    private fun mapRelatedUsers(
        relatedUsers: List<PeopleRelatedUserModel>
    ): List<PeoplesContentUiEntity> {
        if (relatedUsers.isEmpty()) return emptyList()
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(
            RecommendedPeopleListUiEntity(
                recommendedPeopleList = relatedUsers.toRelatedUsersUiEntity(),
                showPossibleFriendsText = true
            )
        )
        return listResult
    }

    private fun createHeaderEntity(
        text: String,
        @DrawableRes drawableTextRes: Int? = null
    ) = HeaderUiEntity(
        text = text,
        textSize = 22,
        textDrawable = drawableTextRes
    )
}
