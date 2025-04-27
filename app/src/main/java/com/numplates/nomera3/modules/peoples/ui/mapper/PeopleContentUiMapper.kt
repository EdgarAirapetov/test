package com.numplates.nomera3.modules.peoples.ui.mapper

import android.content.Context
import androidx.annotation.DrawableRes
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.getAge
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleBloggerPostModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggersPlaceHolderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FindPeoplesUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FriendFindContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleSyncContactsUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import javax.inject.Inject

private const val SHIMMER_ITEM_COUNT = 3
private const val MIN_SUBSCRIBERS_COUNT = 999

// Минимальное кол-во постов блогера
private const val MIN_MEDIA_CONTENT_LIST_SIZE = 3
private const val LABEL_TEXT_SIZE_SEARCH = 18
private const val LABEL_TEXT_SIZE_PEOPLE = 22

class PeopleContentUiMapper @Inject constructor(
    private val appContext: Context,
    private val featureTogglesContainer: FeatureTogglesContainer
) {

    fun createDefaultContent(
        allowSyncContacts: Boolean,
        lastRecentItems: List<RecentUserUiModel>? = null,
        showLastRecentItems: Boolean = true,
        showPeoples: Boolean = true,
        createForSearch: Boolean = false
    ): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        if (allowSyncContacts.not() && showPeoples) {
            listResult.add(createContactSyncUiEntity())
        }
        if (!lastRecentItems.isNullOrEmpty() && showLastRecentItems) {
            listResult.add(createRecentPeopleUiEntity(lastRecentItems))
        }
        if (!showPeoples) return listResult
        listResult.add(createHeaderEntity(appContext.getString(R.string.add_friends), createForSearch))
        listResult.addAll(getFindFriendsContent(createForSearch))
        listResult.addAll(getShimmerContent(createForSearch))
        return listResult
    }

    /**
     * Возвращает уже результат контента с данными, которые пришли
     */
    fun createPeopleContent(
        peopleApprovedUserModels: List<PeopleApprovedUserModel>,
        peopleRelatedUserModels: List<PeopleRelatedUserModel>,
        myUserId: Long,
        allowSyncContacts: Boolean,
        showLastRecentItems: Boolean = true,
        recentUsers: List<RecentUserUiModel>? = null,
        showPeoples: Boolean = true,
        createForSearch: Boolean = false
    ): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        if (allowSyncContacts.not() && showPeoples) {
            listResult.add(createContactSyncUiEntity())
        }
        if (!recentUsers.isNullOrEmpty() && showLastRecentItems) {
            listResult.add(createRecentPeopleUiEntity(recentUsers))
        }
        if (!showPeoples) return listResult
        if (!IS_APP_REDESIGNED) {
            listResult.add(createHeaderEntity(appContext.getString(R.string.add_friends), createForSearch))
        }
        if (peopleRelatedUserModels.isNotEmpty()) {
            val mappedRelatedUsers = mapRelatedUsers(peopleRelatedUserModels, !createForSearch)
            listResult.addAll(mappedRelatedUsers)
        }
        if (IS_APP_REDESIGNED) {
            listResult.add(createHeaderEntity(appContext.getString(R.string.add_friends), createForSearch))
        }
        listResult.addAll(getFindFriendsContent(createForSearch))
        listResult.add(
            createHeaderEntity(
                text = appContext.getString(R.string.general_recommendations),
                createForSearch = createForSearch,
                drawableTextRes = if (createForSearch) null else R.drawable.ic_info_grey_people
            )
        )
        if (peopleApprovedUserModels.any { it.posts.size >= MIN_MEDIA_CONTENT_LIST_SIZE }) {
            val mappedTopUsers = mapFromApprovedUsers(
                approvedUsers = peopleApprovedUserModels,
                myUserId = myUserId
            )
            listResult.addAll(mappedTopUsers)
        } else {
            listResult.add(createBloggersPlaceHolderEntity())
        }
        return listResult
    }

    fun mapFromApprovedUsers(
        approvedUsers: List<PeopleApprovedUserModel>,
        myUserId: Long
    ): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        approvedUsers.forEach loop@ { user ->
            if (user.posts.size < MIN_MEDIA_CONTENT_LIST_SIZE) return@loop
            val approvedUser = createApprovedUser(
                user = user,
                myUserId = myUserId
            )
            listResult.add(approvedUser)
            listResult.add(
                createMediaContentEntity(
                    rootUserId = user.userId,
                    posts = user.posts,
                    user = approvedUser
                )
            )
        }
        return listResult
    }

    fun mapRelatedUsers(
        relatedUsers: List<PeopleRelatedUserModel>,
        showPossibleFriendsText: Boolean
    ): List<PeoplesContentUiEntity> {
        if (relatedUsers.isEmpty()) return emptyList()
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(
            RecommendedPeopleListUiEntity(
                recommendedPeopleList = relatedUsers.toRelatedUsersUiEntity(),
                showPossibleFriendsText = showPossibleFriendsText
            )
        )
        return listResult
    }

    fun mapPaginationRecommendedListToUiList(
        currentList: List<PeoplesContentUiEntity>,
        newList: List<PeopleRelatedUserModel>,
        rootListPosition: Int
    ): List<PeoplesContentUiEntity> {
        val listResult = currentList.toMutableList()
        val newMappedList = newList.toRelatedUsersUiEntity()
        if (rootListPosition >= listResult.size) return listResult
        val currentInnerList =
            (listResult[rootListPosition] as? RecommendedPeopleListUiEntity)?.recommendedPeopleList?.toMutableList()
                ?: mutableListOf()
        currentInnerList.addAll(newMappedList)
        val newEntity = (currentList[rootListPosition] as? RecommendedPeopleListUiEntity)?.copy(
            recommendedPeopleList = currentInnerList
        ) ?: return emptyList()
        listResult[rootListPosition] = newEntity
        return listResult
    }

    fun getUpdatedUserFriendStatusListById(
        currentList: List<PeoplesContentUiEntity>,
        selectedUserId: Long,
        isUserSubscribed: Boolean,
        showPossibleFriendsText: Boolean
    ): List<PeoplesContentUiEntity> {
        if (currentList.isEmpty()) return emptyList()
        val recommendations = currentList
            .filterIsInstance<RecommendedPeopleListUiEntity>()
            .firstOrNull()
        val currentRelatedUsers = recommendations?.recommendedPeopleList ?: emptyList()
        if (currentRelatedUsers.isEmpty()) return currentList
        val newRelated = ArrayList(currentRelatedUsers.map { model ->
            if (model.userId == selectedUserId) {
                model.copy(hasFriendRequest = isUserSubscribed)
            } else {
                model
            }
        })
        val newRelatedList = RecommendedPeopleListUiEntity(
            recommendedPeopleList = newRelated,
            showPossibleFriendsText = showPossibleFriendsText
        )
        val listResult = currentList.toMutableList().map { uiEntity ->
            if (uiEntity is RecommendedPeopleListUiEntity) {
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

    private fun getFindFriendsContent(createForSearch: Boolean): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        if (!createForSearch) {
            listResult.add(
                FindPeoplesUiEntity(
                    label = appContext.getString(R.string.search_by_name),
                    description = appContext.getString(R.string.find_friends_in_whole_world),
                    icon = if (IS_APP_REDESIGNED) R.drawable.ic_outlined_search_m else R.drawable.ic_search_ui_purple,
                    contentType = FriendFindContentType.FIND_FRIENDS
                )
            )
        }
        if (featureTogglesContainer.shakeFeatureToggle.isEnabled) {
            listResult.add(
                FindPeoplesUiEntity(
                    label = appContext.getString(R.string.shake),
                    description = appContext.getString(R.string.shake_your_phone_description),
                    icon = if (IS_APP_REDESIGNED) R.drawable.ic_outlined_bump_m else R.drawable.ic_bump_ui_purple,
                    contentType = FriendFindContentType.BUMP,
                    isNeedToDrawSeparator = !createForSearch
                )
            )
        }
        if (!createForSearch && !IS_APP_REDESIGNED) {
            listResult.add(
                FindPeoplesUiEntity(
                    label = appContext.getString(R.string.tab_invite_friends),
                    description = appContext.getString(R.string.invite_friends_and_get_vip),
                    icon = R.drawable.ic_invite_friends_ui_purple,
                    contentType = FriendFindContentType.INVITE_FRIENDS,
                    isNeedToDrawSeparator = false
                )
            )
        }
        return listResult
    }

    private fun getShimmerContent(createForSearch: Boolean): List<PeoplesContentUiEntity> {
        val listResult = mutableListOf<PeoplesContentUiEntity>()
        listResult.add(
            createHeaderEntity(
                text = appContext.getString(
                    R.string.general_recommendations
                ),
                createForSearch = createForSearch,
                drawableTextRes = if (createForSearch) null else R.drawable.ic_info_grey_people
            )
        )
        repeat(SHIMMER_ITEM_COUNT) {
            listResult.add(PeoplesShimmerUiEntity)
        }
        return listResult
    }

    private fun createBloggersPlaceHolderEntity() = BloggersPlaceHolderUiEntity

    private fun createHeaderEntity(
        text: String,
        createForSearch: Boolean,
        @DrawableRes drawableTextRes: Int? = null
    ) = HeaderUiEntity(
        text = text,
        textSize = if (createForSearch) LABEL_TEXT_SIZE_SEARCH else LABEL_TEXT_SIZE_PEOPLE,
        textDrawable = drawableTextRes
    )

    private fun createBloggerVideoUiEntity(
        user: PeopleBloggerPostModel,
        rootUser: PeopleInfoUiEntity
    ): BloggerMediaContentUiEntity {
        return BloggerMediaContentUiEntity.BloggerVideoContentUiEntity(
            rootUser = rootUser,
            videoDuration = user.duration ?: 0,
            preview = user.preview.orEmpty(),
            videoUrl = user.mediaContentUrl,
            postId = user.id,
        )
    }

    private fun createBloggerImageUiEntity(
        user: PeopleBloggerPostModel,
        rootUser: PeopleInfoUiEntity
    ): BloggerMediaContentUiEntity.BloggerImageContentUiEntity {
        return BloggerMediaContentUiEntity.BloggerImageContentUiEntity(
            rootUser = rootUser,
            postId = user.id,
            imageUrl = user.mediaContentUrl
        )
    }

    private fun createMediaContentEntity(
        rootUserId: Long,
        user: PeopleInfoUiEntity,
        posts: List<PeopleBloggerPostModel>
    ): PeoplesContentUiEntity {
        return BloggerMediaContentListUiEntity(
            userId = rootUserId,
            bloggerPostList = posts.toBloggerMediaUiEntity(
                rootUserId = rootUserId,
                rootUser = user
            )
        )
    }

    private fun List<PeopleBloggerPostModel>.toBloggerMediaUiEntity(
        rootUserId: Long,
        rootUser: PeopleInfoUiEntity
    ): List<BloggerMediaContentUiEntity> {
        val listResult = mutableListOf<BloggerMediaContentUiEntity>()
        this.forEach { user ->
            when (user.mediaContentType) {
                MEDIA_VIDEO -> {
                    listResult.add(
                        createBloggerVideoUiEntity(
                            user = user,
                            rootUser = rootUser
                        )
                    )
                }
                MEDIA_IMAGE -> {
                    listResult.add(
                        createBloggerImageUiEntity(
                            rootUser = rootUser,
                            user = user
                        )
                    )
                }
            }
        }
        listResult.add(
            createMediaPlaceholderUiEntity(
                userId = rootUserId,
                user = rootUser
            )
        )
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
            hasMoments = user.hasMoments ?: false,
            hasNewMoments = user.hasNewMoments ?: false
        )
    }

    private fun createRecentPeopleUiEntity(recentUsers: List<RecentUserUiModel>): RecentUsersUiEntity {
        return RecentUsersUiEntity(recentUsers)
    }

    /**
     * Данный метод создает медиа контент "Смотреть все".
     */
    private fun createMediaPlaceholderUiEntity(
        userId: Long,
        user: PeopleInfoUiEntity
    ): BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity {
        return BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity(
            userId = userId,
            placeholderText = appContext.getString(R.string.watch_all),
            placeholderDrawableRes = R.drawable.ic_gallery_road,
            user = user
        )
    }

    private fun createContactSyncUiEntity() = PeopleSyncContactsUiModel
}
