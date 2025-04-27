package com.numplates.nomera3.modules.moments.show.data.mapper

import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoViewUiModel
import com.numplates.nomera3.modules.moments.show.domain.MomentGroupModel
import com.numplates.nomera3.modules.moments.show.domain.MomentItemModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItemType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import javax.inject.Inject

class MomentsUiMapper @Inject constructor(
    private val getUserUidUseCase: GetUserUidUseCase
) {

    fun mapMomentToMomentGroup(momentItemUiModel: MomentItemUiModel) = MomentGroupUiModel(
        id = ID_SINGLE_MOMENT_GROUP,
        moments = listOf(momentItemUiModel),
        userId = momentItemUiModel.userId,
        isMine = momentItemUiModel.userId == getUserUidUseCase.invoke(),
        placeholder = null
    )

    fun mapToViewUiModel(model: MomentInfoModel?): MomentInfoViewUiModel? {
        if (model == null) return null
        return MomentInfoViewUiModel(model.momentGroups.toMomentGroupUiModel())
    }

    fun mapToViewItemUiModel(model: MomentItemModel?): MomentItemUiModel? {
        if (model == null) return null
        return model.toMomentItemUiModel()
    }

    fun mapToCarouselUiModel(
        model: MomentInfoModel?,
        pagingTicket: String?,
        addCreateMomentItem: Boolean,
        isPagingUsed: Boolean
    ): MomentInfoCarouselUiModel? {
        if (model == null) return null
        val itemType = if (model.isPlaceType) MomentCarouselItemType.Place else MomentCarouselItemType.User
        val list = model.momentGroups
            .filter { it.moments.isNotEmpty() }
            .toMomentCarouselGroupItem(addCreateMomentItem = addCreateMomentItem, itemType = itemType)
            .toMutableList()
        if (isPagingUsed) list.add(MomentCarouselItem.UserShimmerItem)
        return MomentInfoCarouselUiModel(
            useCreateMomentItem = addCreateMomentItem,
            momentsCarouselList = list,
            pagingTicket = pagingTicket
        )
    }

    companion object {
        const val ID_SINGLE_MOMENT_GROUP = -2L
    }
}

private fun List<MomentGroupModel>.toMomentCarouselGroupItem(
    addCreateMomentItem: Boolean,
    itemType: MomentCarouselItemType
): List<MomentCarouselItem> {
    return this.map {
        it.toMomentCarouselGroupItem(addCreateMomentItem = addCreateMomentItem, itemType = itemType)
    }
}

private fun List<MomentItemModel>.toMomentItemUiModel(): List<MomentItemUiModel> {
    return this.map {
        it.toMomentItemUiModel()
    }
}

private fun List<MomentGroupModel>.toMomentGroupUiModel(): List<MomentGroupUiModel> {
    return this.map {
        it.toMomentGroupUiModel()
    }
}

private fun MomentGroupModel.toMomentCarouselGroupItem(
    addCreateMomentItem: Boolean,
    itemType: MomentCarouselItemType
): MomentCarouselItem {
    return if (addCreateMomentItem && isMine) {
        MomentCarouselItem.MomentCreateItem(
            group = toMomentGroupUiModel()
        )
    } else {
        MomentCarouselItem.MomentGroupItem(
            group = toMomentGroupUiModel(),
            type = itemType
        )
    }
}

private fun MomentGroupModel.toMomentGroupUiModel(): MomentGroupUiModel {
    return MomentGroupUiModel(
        id = userId,
        userId = userId,
        moments = moments.toMomentItemUiModel(),
        placeholder = null,
        isMine = isMine
    )
}

private fun MomentItemModel.toMomentItemUiModel(): MomentItemUiModel {
    return MomentItemUiModel(
        id = id,
        isActive = isActive,
        isDeleted = isDeleted,
        isAccessDenied = isAccessDenied,
        createdAt = createdAt,
        isViewed = isViewed,
        place = place,
        contentUrl = contentUrl,
        contentType = contentType,
        contentPreview = contentPreview,
        doNotShowUser = doNotShowUser,
        reactions = reactions,
        commentAvailability = commentAvailability,
        commentsCount = commentsCount,
        repostsCount = repostsCount,
        viewsCount = viewsCount,
        userAvatarSmall = userAvatarSmall,
        userName = userName,
        userAccountColor = userAccountColor,
        userAccountType = userAccountType,
        userId = userId,
        userGender = userGender,
        userApproved = userApproved,
        userTopContentMaker = userTopContentMaker,
        isSubscribedToUser = isSubscribedToUser,
        isUserBlackListMe = isUserBlackListMe,
        isUserBlackListByMe = isUserBlackListByMe,
        meCanCommentMoment = meCanCommentMoment,
        media = media,
        adultContent = adultContent
    )
}

