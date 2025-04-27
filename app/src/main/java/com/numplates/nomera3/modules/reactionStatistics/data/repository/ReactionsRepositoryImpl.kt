package com.numplates.nomera3.modules.reactionStatistics.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.reactionStatistics.data.api.ReactionsApi
import com.numplates.nomera3.modules.reactionStatistics.data.mapper.ReactionsDataMapper
import com.numplates.nomera3.modules.reactionStatistics.domain.models.ReactionRootModel
import com.numplates.nomera3.modules.reactionStatistics.domain.models.viewers.ViewersRootModel
import javax.inject.Inject

@AppScope
class ReactionsRepositoryImpl @Inject constructor(
    private val reactionsApi: ReactionsApi,
    private val mapper: ReactionsDataMapper
) : ReactionsRepository {

    override suspend fun getPostReactions(
        postId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel> {
        val reactionsResponse = reactionsApi.getPostReactions(
            postId = postId,
            reaction = reaction,
            limit = limit,
            offset = offset
        )
        return mapper.mapReactionRootDtoToReactionRootModel(reactionsResponse.data)
    }

    override suspend fun getCommentReactions(
        commentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel> {
        val reactionsResponse = reactionsApi.getCommentReactions(
            commentId = commentId,
            reaction = reaction,
            limit = limit,
            offset = offset
        )

        return mapper.mapReactionRootDtoToReactionRootModel(reactionsResponse.data)
    }

    override suspend fun getMomentReactions(
        momentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel> {
        val reactionsResponse = reactionsApi.getMomentReactions(
            momentId = momentId,
            reaction = reaction,
            limit = limit,
            offset = offset
        )

        return mapper.mapReactionRootDtoToReactionRootModel(reactionsResponse.data)
    }

    override suspend fun getMomentViewers(
        momentId: Long,
        limit: Int,
        offset: Int
    ): ViewersRootModel {
        val viewsResponse = reactionsApi.getMomentViewers(
            momentId = momentId,
            limit = limit,
            offset = offset
        )

        return mapper.mapViewersRootDtoToViewersRootModel(viewsResponse.data)
    }

    override suspend fun getMomentCommentReactions(
        commentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ): List<ReactionRootModel> {
        val reactionsResponse = reactionsApi.getMomentCommentReactions(
            commentId = commentId,
            reaction = reaction,
            limit = limit,
            offset = offset
        )

        return mapper.mapReactionRootDtoToReactionRootModel(reactionsResponse.data)
    }
}
