package com.numplates.nomera3.modules.reaction.data

import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource

data class MeeraReactionUpdate(
    val type: Type,
    val reactionSource: MeeraReactionSource,
    val reaction: ReactionType,
    val reactionList: List<ReactionEntity>
) {
    enum class Type {
        Add,
        Remove
    }
}
