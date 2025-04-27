package com.numplates.nomera3.modules.reaction.ui.util

object ReactionStyleMapper {
    fun map(isMine: Int): ReactionCounterStyle {
        return if (isMine == 1) {
            ReactionCounterStyle.Mine
        } else {
            ReactionCounterStyle.Other
        }
    }

    fun map(isMine: Boolean): ReactionCounterStyle {
        return if (isMine) {
            ReactionCounterStyle.Mine
        } else {
            ReactionCounterStyle.Other
        }
    }
}