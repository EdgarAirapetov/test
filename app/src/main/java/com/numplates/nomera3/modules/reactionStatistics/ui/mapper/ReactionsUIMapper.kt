package com.numplates.nomera3.modules.reactionStatistics.ui.mapper

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionRootModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers.ViewersRootModel
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import javax.inject.Inject

class ReactionsUIMapper @Inject constructor() {

    fun mapToReactionTabUi(items: List<ReactionRootModel>): List<ReactionTabUiEntity> {
        val mappedItems = items.map { ReactionTabUiEntity(listOf(it.reaction), it.count) }
        val all = mappedItems.first().copy(reactions = items.drop(1).take(3).map { it.reaction })

        return when (mappedItems.size) {
            1 -> emptyList()
            2 -> mappedItems.subList(1, 2)
            else -> mappedItems.mapIndexed { ind, item -> return@mapIndexed if (ind == 0) all else item }
        }
    }

    fun mapToReactionWithViewsTabUi(viewsCount: Long, items: List<ReactionRootModel>): List<ReactionTabUiEntity> {
        return if (viewsCount > 0) {
            listOf(ReactionTabUiEntity(isViewersTab = true)) + mapToReactionTabUi(items)
        } else {
            mapToReactionTabUi(items)
        }
    }

    fun mapToReactionUserUi(items: List<ReactionModel>): List<ReactionUserUiEntity> {
        return items.map {
            ReactionUserUiEntity(
                reaction = ReactionType.getByString(it.reaction) ?: ReactionType.GreenLight,
                userId = it.userId,
                name = it.user.name,
                username = it.user.uniqname,
                avatar = it.user.avatar,
                accountType = it.user.accountType,
                accountApproved = it.user.approved.toBoolean(),
                topContentMaker = it.user.topContentMaker.toBoolean(),
                gender = it.user.gender,
                frameColor = it.user.accountColor
            )
        }
    }

    fun mapToViewerUserUi(items: ViewersRootModel): List<ReactionUserUiEntity> {
        return items.viewers.map {
            var reaction: ReactionType? = null
            it.reaction?.let { react ->
                reaction = ReactionType.getByString(react)
            }
            ReactionUserUiEntity(
                reaction = reaction,
                userId = it.user.id,
                name = it.user.name,
                username = it.user.uniqname,
                avatar = it.user.avatar,
                accountType = it.user.accountType,
                accountApproved = it.user.approved.toBoolean(),
                topContentMaker = it.user.topContentMaker.toBoolean(),
                gender = it.user.gender,
                frameColor = it.user.accountColor
            )
        }
    }
}
