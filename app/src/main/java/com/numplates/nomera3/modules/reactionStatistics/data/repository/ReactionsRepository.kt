package com.numplates.nomera3.modules.reactionStatistics.data.repository

import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionRootModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers.ViewersRootModel

interface ReactionsRepository {
    suspend fun getPostReactions(
        postId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel>

    suspend fun getCommentReactions(
        commentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel>

    suspend fun getMomentReactions(
        momentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel>

    suspend fun getMomentViewers(
        momentId: Long,
        limit: Int,
        offset: Int
    ): ViewersRootModel

    suspend fun getMomentCommentReactions(
        commentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel>
}
