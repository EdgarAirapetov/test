package com.numplates.nomera3.modules.reaction.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseError
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionApi
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.domain.MomentDeletedException
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import javax.inject.Inject

class AddReactionUseCase @Inject constructor(
    val api: ReactionApi,
    private val reactionRepository: ReactionRepository
    ) {

    suspend fun execute(
        reactionSource: ReactionSource,
        reaction: ReactionType
    ): List<ReactionEntity> {
        return when (reactionSource) {
            is ReactionSource.PostComment -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
            is ReactionSource.CommentBottomMenu -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
            is ReactionSource.Post -> {
                executePost(reactionSource.postId, reaction.value)
            }
            is ReactionSource.Moment -> executeMoment(
                momentId = reactionSource.momentId,
                reaction = reaction.value
            )
            is ReactionSource.MomentComment -> {
                executeMomentCommentReaction(reactionSource.commentId, reaction.value)
            }
            is ReactionSource.CommentBottomSheet -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
        }
    }

    suspend fun executeMeera(
        reactionSource: MeeraReactionSource,
        reaction: ReactionType
    ): List<ReactionEntity> {
        return when (reactionSource) {
            is MeeraReactionSource.PostComment -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
            is MeeraReactionSource.CommentBottomMenu -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
            is MeeraReactionSource.Post -> {
                executePost(reactionSource.postId, reaction.value)
            }
            is MeeraReactionSource.Moment -> executeMoment(
                momentId = reactionSource.momentId,
                reaction = reaction.value
            )
            is MeeraReactionSource.MomentComment -> {
                executeMomentCommentReaction(reactionSource.commentId, reaction.value)
            }
            is MeeraReactionSource.CommentBottomSheet -> {
                executeComment(reactionSource.commentId, reaction.value)
            }
        }
    }

    private suspend fun executeMomentCommentReaction(
        commentId: Long,
        reaction: String
    ): List<ReactionEntity> {
        val response = api.addMomentCommentReaction(commentId, reaction)

        response.err?.let { throw proceedError(it) }

        return response.data.reactions
    }

    private suspend fun executeComment(commentId: Long, reaction: String): List<ReactionEntity> {
        val response = api.addCommentReaction(commentId, reaction)

        response.err?.let { throw proceedError(it) }

        return response.data.comment.reactions
    }

    private fun proceedError(error: ResponseError): Throwable {
        val code = error.code
        val message = error.userMessage ?: ""

        return when (code) {
            AlreadyDeletedException.CODE -> {
                AlreadyDeletedException(message)
            }
            MomentDeletedException.CODE -> {
                MomentDeletedException(message)
            }
            else -> {
                IllegalStateException(message)
            }
        }
    }

    private suspend fun executePost(postId: Long, reaction: String): List<ReactionEntity> {
        val response = api.addPostReaction(postId, reaction)

        response.err?.let { throw proceedError(it) }
        response.data?.post?.let(reactionRepository::setReactionPostUpdate)
        return response.data!!.post.reactions ?: emptyList()
    }

    private suspend fun executeMoment(momentId: Long, reaction: String): List<ReactionEntity> {
        val response = api.addMomentReaction(momentId = momentId, reaction = reaction)
        response.err?.let { throw proceedError(it) }
        return emptyList()
    }

    class AlreadyDeletedException(override val message: String) : IllegalStateException(message) {
        companion object {
            const val CODE = 4920
        }
    }
}
