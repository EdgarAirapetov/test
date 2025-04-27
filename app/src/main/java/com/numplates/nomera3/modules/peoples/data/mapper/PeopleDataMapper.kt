package com.numplates.nomera3.modules.peoples.data.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.db.models.people.PeopleApprovedUserDbModel
import com.meera.db.models.people.PeopleRelatedUserDbModel
import com.meera.db.models.people.PeopleUserPostDbModel
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.peoples.data.dto.ApprovedUserPostDto
import com.numplates.nomera3.modules.peoples.data.dto.ApprovedUsersDto
import com.numplates.nomera3.modules.peoples.data.dto.RelatedUsersDto
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleBloggerPostModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import com.numplates.nomera3.presentation.model.MutualUser
import javax.inject.Inject

private const val DEFAULT_USER_SUBSCRIBED = false
private const val DEFAULT_HAS_FRIEND_REQUEST = false

class PeopleDataMapper @Inject constructor() {

    fun mapApprovedDbModelToApprovedUsers(
        approvedUsers: List<PeopleApprovedUserDbModel>
    ): List<PeopleApprovedUserModel> = approvedUsers.map { user ->
        PeopleApprovedUserModel(
            userId = user.userId,
            subscribersCount = user.subscribersCount,
            userName = user.userName,
            accountColor = user.accountColor,
            accountType = user.accountType,
            approved = user.approved,
            topContentMaker = user.topContentMaker,
            avatarSmall = user.avatarSmall,
            uniqueName = user.uniqueName,
            settingsFlags = user.settingsFlags,
            posts = user.posts?.toBloggerPostsFromDb() ?: mutableListOf(),
            isUserSubscribed = user.isUserSubscribed,
            hasMoments = user.hasMoments,
            hasNewMoments = user.hasNewMoments
        )
    }

    fun mapApprovedDtoModelToApprovedUsers(
        approvedUsers: List<ApprovedUsersDto>
    ): List<PeopleApprovedUserModel> = approvedUsers.map { user ->
        PeopleApprovedUserModel(
            userId = user.userId ?: 0,
            subscribersCount = user.subscribersCount ?: 0,
            userName = user.userName.orEmpty(),
            accountColor = user.accountColor,
            accountType = user.accountType ?: 0,
            approved = user.approved,
            topContentMaker = user.topContentMarker,
            avatarSmall = user.avatar.orEmpty(),
            uniqueName = user.uniqueName.orEmpty(),
            settingsFlags = user.settingsFlags,
            posts = user.posts?.toBloggerPostsFromDto() ?: emptyList(),
            isUserSubscribed = DEFAULT_USER_SUBSCRIBED,
            hasMoments = user.moments?.hasMoments.toBoolean(),
            hasNewMoments = user.moments?.hasNewMoments.toBoolean(),
        )
    }

    fun mapApprovedDtoModelToDbModel(
        approvedUsers: List<ApprovedUsersDto>
    ): List<PeopleApprovedUserDbModel> = approvedUsers.map { user ->
        PeopleApprovedUserDbModel(
            userId = user.userId ?: 0,
            subscribersCount = user.subscribersCount ?: 0,
            userName = user.userName.orEmpty(),
            accountColor = user.accountColor,
            accountType = user.accountType ?: 0,
            approved = user.approved,
            topContentMaker = user.topContentMarker,
            avatarSmall = user.avatar.orEmpty(),
            uniqueName = user.uniqueName.orEmpty(),
            posts = user.posts?.toBloggerPostsFromDtoToDb(),
            settingsFlags = user.settingsFlags,
            isUserSubscribed = DEFAULT_USER_SUBSCRIBED,
            hasMoments = user.moments?.hasMoments.toBoolean(),
            hasNewMoments = user.moments?.hasNewMoments.toBoolean(),
        )
    }

    fun mapRelatedUsersDtoModelToDbModel(
        relatedUsers: List<RelatedUsersDto>
    ): List<PeopleRelatedUserDbModel> = relatedUsers.map { user ->
        PeopleRelatedUserDbModel(
            userId = user.id ?: 0,
            name = user.name.orEmpty(),
            accountColor = user.accountColor ?: 0,
            accountType = user.accountType ?: 0,
            mutualFriends = user.toUserSimple(),
            countryId = user.country?.id ?: 0,
            mutualTotalCount = user.mutualTotalCount ?: 0,
            approved = user.approved ?: 0,
            avatar = user.avatar.orEmpty(),
            birthday = user.birthday,
            gender = user.gender ?: 0,
            settingsFlags = user.settingsFlags,
            country =  user.country,
            city = user.city,
            hasFriendRequest = DEFAULT_HAS_FRIEND_REQUEST,
            topContentMaker = user.topContentMaker
        )
    }.sortedWith(compareByDescending(PeopleRelatedUserDbModel::mutualTotalCount)
        .thenByDescending(PeopleRelatedUserDbModel::userId))

    fun mapRelatedUsersDtoModel(
        relatedUsers: List<RelatedUsersDto>
    ): List<PeopleRelatedUserModel> = relatedUsers.map { user ->
        PeopleRelatedUserModel(
            userId = user.id ?: 0,
            name = user.name.orEmpty(),
            accountType = user.accountType ?: 0,
            approved = user.approved ?: 0,
            accountColor = user.accountColor ?: 0,
            countryId = user.countryId ?: 0,
            cityId = user.city?.id ?: 0,
            cityName = user.city?.name.orEmpty(),
            countryName = user.country?.name.orEmpty(),
            gender = user.gender ?: 0,
            settingsFlags = user.settingsFlags,
            mutualTotalCount = user.mutualTotalCount ?: 0,
            avatar = user.avatar.orEmpty(),
            birthday = user.birthday,
            mutualFriends = MutualFriendsUiEntity(
                mutualFriends = user.mutualFriends?.map {
                    MutualUser(
                        id = it.id ?: -1,
                        name = it.name ?: "",
                        avatarSmall = it.avatar ?: ""
                    )
                } ?: emptyList(),
                accountTypeEnum = AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                moreCount = 0
            ),
            hasFriendRequest = DEFAULT_HAS_FRIEND_REQUEST,
            topContentMaker = user.topContentMaker.toBoolean()
        )
    }.sortedWith(compareByDescending(PeopleRelatedUserModel::mutualTotalCount)
        .thenByDescending(PeopleRelatedUserModel::userId))

    fun mapRelatedUsersDbModel(
        relatedUsers: List<PeopleRelatedUserDbModel>
    ): List<PeopleRelatedUserModel> = relatedUsers.map { user ->
        PeopleRelatedUserModel(
            userId = user.userId,
            name = user.name,
            accountType = user.accountType,
            approved = user.approved,
            accountColor = user.accountColor,
            countryId = user.countryId,
            cityId = user.city?.id ?: 0,
            cityName = user.city?.name.orEmpty(),
            countryName = user.country?.name.orEmpty(),
            gender = user.gender,
            settingsFlags = user.settingsFlags,
            mutualTotalCount = user.mutualTotalCount,
            avatar = user.avatar,
            birthday = user.birthday,
            mutualFriends = MutualFriendsUiEntity(
                mutualFriends = user.mutualFriends?.map {
                    MutualUser(
                        id = it.userId,
                        name = it.name ?: "",
                        avatarSmall = it.avatarSmall ?: ""
                    )
                } ?: emptyList(),
                accountTypeEnum = AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                moreCount = 0
            ),
            hasFriendRequest = user.hasFriendRequest,
            topContentMaker = user.topContentMaker.toBoolean()
        )
    }.sortedWith(compareByDescending(PeopleRelatedUserModel::mutualTotalCount)
        .thenByDescending(PeopleRelatedUserModel::userId))


    private fun List<PeopleUserPostDbModel>.toBloggerPostsFromDb(): List<PeopleBloggerPostModel> = this.map { user ->
        PeopleBloggerPostModel(
            userId = user.id,
            isAdultContent = user.isAdultContent,
            cityId = user.cityId,
            countryId = user.countryId,
            createdAt = user.createdAt,
            isDeleted = user.isDeleted,
            dislikes = user.dislikes,
            moderated = user.moderated,
            preview = user.preview,
            duration = user.duration,
            mediaContentType = user.mediaContentType,
            mediaContentUrl = user.mediaContentUrl,
            isAllowedToComment = user.isAllowedToComment,
            isSubscription = user.isSubscription,
            id = user.id,
            updatedAt = user.updatedAt,
            privacy = user.privacy
        )
    }

    private fun List<ApprovedUserPostDto>.toBloggerPostsFromDto(): List<PeopleBloggerPostModel> = this.map { user ->
        PeopleBloggerPostModel(
            userId = user.id ?: 0,
            isAdultContent = user.isAdultContent ?: false,
            cityId = user.cityId,
            countryId = user.countryId ?: 0,
            createdAt = user.createdAt ?: 0,
            isDeleted = user.isDeleted ?: false,
            dislikes = user.dislikes ?: 0,
            moderated = user.moderated.orEmpty(),
            preview = user.asset?.metadata?.preview,
            duration = user.asset?.metadata?.duration,
            mediaContentType = user.asset?.type.orEmpty(),
            mediaContentUrl = user.asset?.url.orEmpty(),
            isAllowedToComment = user.isAllowedToComment ?: false,
            isSubscription = user.isSubscription ?: false,
            id = user.id ?: 0,
            updatedAt = user.updatedAt ?: 0,
            privacy = user.privacy.orEmpty()
        )
    }

    private fun List<ApprovedUserPostDto>.toBloggerPostsFromDtoToDb(): List<PeopleUserPostDbModel> = this.map { user ->
        PeopleUserPostDbModel(
            userId = user.id ?: 0,
            isAdultContent = user.isAdultContent ?: false,
            cityId = user.cityId,
            countryId = user.countryId ?: 0,
            createdAt = user.createdAt ?: 0,
            isDeleted = user.isDeleted ?: false,
            dislikes = user.dislikes ?: 0,
            moderated = user.moderated.orEmpty(),
            preview = user.asset?.metadata?.preview,
            duration = user.asset?.metadata?.duration,
            mediaContentType = user.asset?.type.orEmpty(),
            mediaContentUrl = user.asset?.url.orEmpty(),
            isAllowedToComment = user.isAllowedToComment ?: false,
            isSubscription = user.isSubscription ?: false,
            id = user.id ?: 0,
            updatedAt = user.updatedAt ?: 0,
            privacy = user.privacy.orEmpty(),
            groupId = user.groupId ?: 0,
            groupName = user.groupName,
            comments = user.comments ?: 0,
            commentAvailability = user.commentAvailability.orEmpty(),
            likes = user.likes ?: 0,
            reposts = user.reposts
        )
    }

    // TODO: BR-20197 Временное решение.. Убрать, когда ветка PO-533_SHAKE пойдет в dev
    private fun RelatedUsersDto.toUserSimple(): List<UserSimple> {
        val listResult = mutableListOf<UserSimple>()
        this.mutualFriends?.forEach { user ->
            listResult.add(
                UserSimple(
                    userId = user.id ?: 0,
                    avatar = user.avatar ?: String.empty(),
                    name = user.name ?: String.empty(),
                    uniqueName = null,
                    accountColor = user.accountColor,
                    accountType = user.accountType,
                    birthday = user.birthday,
                    city = user.city,
                    approved = user.approved ?: 0
                )
            )
        }
        return listResult
    }
}
