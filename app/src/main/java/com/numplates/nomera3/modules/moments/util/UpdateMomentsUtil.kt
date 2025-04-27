package com.numplates.nomera3.modules.moments.util

import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity

fun MomentGroupUiModel.updateMomentReactionsById(
    momentId: Long,
    reactionsList: List<ReactionEntity>
): MomentGroupUiModel? {
    val groupMoments = moments.toMutableList()
    val moment = groupMoments.firstOrNull { it.id == momentId } ?: return null
    val momentIndex = moments.getMomentIndex(moment) ?: return null
    groupMoments[momentIndex] = moment.copy(reactions = reactionsList)
    return this.copy(moments = groupMoments)
}

fun MomentGroupUiModel.updateMomentGroupByMoment(
    newMoment: MomentItemUiModel
): MomentGroupUiModel? {
    val groupMoments = moments.toMutableList()
    val moment = groupMoments.firstOrNull { it.id == newMoment.id } ?: return null
    val momentIndex = groupMoments.getMomentIndex(moment) ?: return null
    groupMoments[momentIndex] = newMoment
    return this.copy(moments = groupMoments)
}

fun List<MomentItemUiModel>.getMomentIndex(moment: MomentItemUiModel): Int? {
    val momentIndex = indexOf(moment)
    return if (momentIndex == -1) null else momentIndex
}
