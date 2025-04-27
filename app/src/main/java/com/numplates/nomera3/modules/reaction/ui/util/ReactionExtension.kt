package com.numplates.nomera3.modules.reaction.ui.util

import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.data.net.isMine

fun List<ReactionEntity>.reactionCount(): Int {
    var result = 0

    forEach { reaction ->
        result += reaction.count
    }

    return result
}

fun List<ReactionEntity>.getMyReaction(): ReactionType? {
    val mineReaction = find { reaction -> reaction.isMine() }
    return ReactionType.getByString(mineReaction?.reactionType)
}
