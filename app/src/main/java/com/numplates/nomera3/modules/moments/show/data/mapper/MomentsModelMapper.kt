package com.numplates.nomera3.modules.moments.show.data.mapper

import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.feed.domain.mapper.toUiMedia
import com.numplates.nomera3.modules.moments.show.data.GetMomentGroupsResponseDto
import com.numplates.nomera3.modules.moments.show.data.MomentGroupDto
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.moments.show.data.MomentLinkResponseDto
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.modules.moments.show.domain.MomentGroupModel
import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import com.numplates.nomera3.modules.moments.show.domain.MomentLinkModel
import javax.inject.Inject

class MomentsModelMapper @Inject constructor() {

    fun mapFromDtoToModel(dto: MomentLinkResponseDto): MomentLinkModel {
        return MomentLinkModel(deepLinkUrl = dto.deepLinkUrl)
    }

    fun mapFromDtoToModel(dto: GetMomentGroupsResponseDto): MomentInfoModel {
        return MomentInfoModel(
            momentGroups = dto.momentGroups.toMomentGroupModel(),
            session = dto.session
        )
    }

    fun mapFromDtoToModel(dto: MomentGroupDto): MomentGroupModel {
        return dto.toMomentGroupModel()
    }

    fun mapFromDtoToModel(dto: MomentItemDto): MomentItemModel {
        return dto.toMomentItemModel()
    }
}

private fun List<MomentGroupDto>.toMomentGroupModel(): List<MomentGroupModel> {
    return this.map { it.toMomentGroupModel() }
}

private fun List<MomentItemDto>.toMomentItemModel(user: UserSimple): List<MomentItemModel> {
    return this.map {
        it.toMomentItemModel(user)
    }
}

private fun MomentGroupDto.toMomentGroupModel(): MomentGroupModel {
    return MomentGroupModel(
        isMine = this.isMine.toBoolean(),
        moments = this.moments.toMomentItemModel(this.user),
        userId = this.userId
    )
}

private fun toCommentsAvailabilityType(commentAvailability: Int): CommentsAvailabilityType {
    return when (commentAvailability) {
        0 -> CommentsAvailabilityType.NOBODY
        1 -> CommentsAvailabilityType.ALL
        else -> CommentsAvailabilityType.FRIENDS
    }
}

private fun MomentItemDto.toMomentItemModel(user: UserSimple? = null): MomentItemModel {
    return MomentItemModel(
        id = id,
        isActive = active.toBoolean(),
        isDeleted = deleted.toBoolean(),
        isAccessDenied = accessDenied.toBoolean(),
        createdAt = createdAt,
        isViewed = viewed.toBoolean(),
        place = place,
        contentUrl = asset?.url,
        contentType = asset?.type,
        contentPreview = asset?.preview,
        doNotShowUser = doNotShowUser.toBoolean(),
        reactions = reactions,
        commentAvailability = toCommentsAvailabilityType(commentAvailability),
        commentsCount = commentsCount,
        repostsCount = repostsCount,
        viewsCount = viewsCount,
        userAvatarSmall = this.user?.avatarSmall ?: user?.avatarSmall,
        userName = this.user?.name ?: user?.name,
        userAccountColor = this.user?.accountColor ?: user?.accountColor,
        userAccountType = this.user?.accountType ?: user?.accountType,
        userId = this.user?.userId ?: user?.userId ?: 0L,
        userGender = this.user?.gender ?: user?.gender,
        userApproved = (this.user?.approved ?: user?.approved).toBoolean(),
        userTopContentMaker = (this.user?.topContentMaker ?:user?.topContentMaker).toBoolean(),
        isSubscribedToUser = (this.user?.settingsFlags?.subscription_on ?: user?.settingsFlags?.subscription_on).toBoolean(),
        isUserBlackListMe = (this.user?.blacklistedMe ?:user?.blacklistedMe).toBoolean(),
        isUserBlackListByMe = (this.user?.blacklistedByMe ?:user?.blacklistedByMe).toBoolean(),
        meCanCommentMoment = iCanComment.toBoolean(),
        media = mediaEntity?.toUiMedia(),
        adultContent = adultContent
    )
}
