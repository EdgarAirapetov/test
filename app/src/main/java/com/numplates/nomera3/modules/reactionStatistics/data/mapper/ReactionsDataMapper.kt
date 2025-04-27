package com.numplates.nomera3.modules.reactionStatistics.data.mapper

import com.numplates.nomera3.modules.reactionStatistics.data.entity.ReactionDto
import com.numplates.nomera3.modules.reactionStatistics.data.entity.ReactionsRootDto
import com.numplates.nomera3.modules.reactionStatistics.data.entity.UserDto
import com.numplates.nomera3.modules.reactionStatistics.data.entity.viewers.ViewerDto
import com.numplates.nomera3.modules.reactionStatistics.data.entity.viewers.ViewersRootDto
import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionRootModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.UserModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers.ViewerModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers.ViewersRootModel
import javax.inject.Inject

class ReactionsDataMapper @Inject constructor() {

    fun mapReactionRootDtoToReactionRootModel(reactionsRoots: List<ReactionsRootDto>): List<ReactionRootModel> {
        return reactionsRoots.map {
            ReactionRootModel(
                count = it.count,
                more = it.more,
                commentId = it.commentId,
                postId = it.postId,
                reaction = it.reaction,
                reactions = mapReactionsDtoToReactionsModel(it.reactions)
            )
        }
    }

    private fun mapReactionsDtoToReactionsModel(reactions: List<ReactionDto>): List<ReactionModel> {
        return reactions.map {
            ReactionModel(
                reaction = it.reaction,
                commentId = it.commentId,
                postId = it.postId,
                userId = it.userId,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                user = mapUserDtoToUserModel(it.user)
            )
        }
    }

    private fun mapUserDtoToUserModel(user: UserDto): UserModel {
        return UserModel(
            accountColor = user.accountColor,
            accountType = user.accountType,
            approved = user.approved,
            avatar = user.avatar,
            birthday = user.birthday,
            city = user.city,
            gender = user.gender,
            id = user.id,
            name = user.name,
            topContentMaker = user.topContentMaker,
            uniqname = user.uniqname
        )
    }

    fun mapViewersRootDtoToViewersRootModel(viewersDto: ViewersRootDto): ViewersRootModel {
        return ViewersRootModel(
                count = viewersDto.count,
                more = viewersDto.more,
                viewers = mapViewerDtoToViewerModel(viewersDto.viewers)
            )
    }

    private fun mapViewerDtoToViewerModel(viewers: List<ViewerDto>): List<ViewerModel> {
        return viewers.map {
            ViewerModel(
                reaction = it.reaction,
                user = mapUserDtoToUserModel(it.user),
                viewedAt = it.viewedAt
            )
        }
    }

}
