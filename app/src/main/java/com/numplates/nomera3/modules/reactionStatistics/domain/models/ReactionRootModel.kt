package com.numplates.nomera3.modules.reactionStatistics.domain.models

data class ReactionRootModel(
    val count: Int,
    val more: Int,
    val commentId: Long,
    val postId: Long,
    val reaction: String,
    val reactions: List<ReactionModel>
)
