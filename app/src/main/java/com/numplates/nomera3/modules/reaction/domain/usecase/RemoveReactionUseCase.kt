package com.numplates.nomera3.modules.reaction.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseError
import com.numplates.nomera3.modules.reaction.data.net.ReactionApi
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.domain.AlreadyDeletedException
import com.numplates.nomera3.modules.reaction.domain.MomentDeletedException
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import javax.inject.Inject

class RemoveReactionUseCase @Inject constructor(
    val api: ReactionApi,
    private val reactionRepository: ReactionRepository
    ) {

    suspend fun execute(
        reactionSource: ReactionSource
    ): List<ReactionEntity> {
        return when (reactionSource) {
            is ReactionSource.PostComment -> {
                executeComment(reactionSource.commentId)
            }
            is ReactionSource.CommentBottomMenu -> {
                executeComment(reactionSource.commentId)
            }
            is ReactionSource.Post -> {
                executePost(reactionSource.postId)
            }
            is ReactionSource.MomentComment -> {
                executeMomentComment(reactionSource.commentId)
            }
            is ReactionSource.Moment -> {
                executeMoment(reactionSource.momentId)
            }
            is ReactionSource.CommentBottomSheet -> {
                executeComment(reactionSource.commentId)
            }
        }
    }

    suspend fun executeMeera(
        reactionSource: MeeraReactionSource
    ): List<ReactionEntity> {
        return when (reactionSource) {
            is MeeraReactionSource.PostComment -> {
                executeComment(reactionSource.commentId)
            }
            is MeeraReactionSource.CommentBottomMenu -> {
                executeComment(reactionSource.commentId)
            }
            is MeeraReactionSource.Post -> {
                executePost(reactionSource.postId)
            }
            is MeeraReactionSource.MomentComment -> {
                executeMomentComment(reactionSource.commentId)
            }
            is MeeraReactionSource.Moment -> {
                executeMoment(reactionSource.momentId)
            }
            is MeeraReactionSource.CommentBottomSheet -> {
                executeComment(reactionSource.commentId)
            }
        }
    }

    private suspend fun executeMomentComment(commentId: Long): List<ReactionEntity> {
        val response = api.removeMomentCommentReaction(commentId)
        response.err?.let { throw proceedError(it) }
        return response.data!!.reactions
    }

    private suspend fun executeComment(commentId: Long): List<ReactionEntity> {
        val response = api.removeCommentReaction(commentId)

        response.err?.let { throw proceedError(it) }

        return response.data!!.comment.reactions
    }

    private suspend fun executePost(postId: Long): List<ReactionEntity> {
        val response = api.removePostReaction(postId)

        response.err?.let { throw proceedError(it) }
        response.data?.post?.let(reactionRepository::setReactionPostUpdate)
        return response.data?.post?.reactions ?: emptyList()
    }

    private suspend fun executeMoment(momentId: Long): List<ReactionEntity> {
        val response = api.removeMomentReaction(momentId)
        response.err?.let { throw proceedError(it) }
        return emptyList()
    }

    private fun proceedError(error: ResponseError): Throwable {
        val code = error.code
        val message = error.userMessage ?: ""

        return when (code) {
            MomentDeletedException.CODE -> MomentDeletedException(message)
            AlreadyDeletedException.CODE -> AlreadyDeletedException(message)
            else -> IllegalStateException(message)
        }
    }
}
