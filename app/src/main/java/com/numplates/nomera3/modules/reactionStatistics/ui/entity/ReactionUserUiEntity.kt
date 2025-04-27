package com.numplates.nomera3.modules.reactionStatistics.ui.entity

import com.numplates.nomera3.modules.reaction.data.ReactionType

data class ReactionUserUiEntity(
    val reaction: ReactionType?,
    val userId: Long,
    val name: String?,
    val username: String?,
    val avatar: String,
    val accountType: Int,
    val accountApproved: Boolean,
    val topContentMaker: Boolean,
    val gender: Int,
    val frameColor: Int
)
