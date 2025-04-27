package com.numplates.nomera3.modules.reactionStatistics.domain.models

data class ReactionModel(
    val reaction: String,
    val commentId: Long,
    val postId: Long,
    val userId: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val user: UserModel
)
