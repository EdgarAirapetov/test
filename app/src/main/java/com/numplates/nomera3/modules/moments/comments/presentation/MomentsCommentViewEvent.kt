package com.numplates.nomera3.modules.moments.comments.presentation

import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate

sealed class MomentsCommentViewEvent {
    object EnableComments : MomentsCommentViewEvent()
    object OnScrollToBottom : MomentsCommentViewEvent()

    object ComplainSuccess : MomentsCommentViewEvent()

    class OnAddUserToBlocked(var userId: Long) : MomentsCommentViewEvent()

    class MarkCommentForDeletion(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : MomentsCommentViewEvent()

    class CancelDeleteComment(
        val originalComment: CommentUIType
    ) : MomentsCommentViewEvent()

    class DeleteComment(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : MomentsCommentViewEvent()

    data class ErrorInnerPagination(
        val data: CommentSeparatorEntity
    ) : MomentsCommentViewEvent()

    data class NewCommentSuccess(
        var myCommentId: Long,
        var beforeMyComment: List<CommentUIType>,
        var afterMyComment: List<CommentUIType>,
        var hasIntersection: Boolean,
        var needSmoothScroll: Boolean,
        var needToShowLastFullComment: Boolean
    ) : MomentsCommentViewEvent()

    data class NewInnerCommentSuccess(
        val parentId: Long,
        val chunk: CommentChunk
    ) : MomentsCommentViewEvent()

    class ErrorDeleteComment(
        val comment: CommentUIType
    ) : MomentsCommentViewEvent()

    object NoInternet : MomentsCommentViewEvent()

    object NoInternetAction : MomentsCommentViewEvent()

    object ErrorPublishMomentComment : MomentsCommentViewEvent()

    data class CommentRestricted(val message: String? = null) : MomentsCommentViewEvent()

    data class ShowTextError(
        val message: String?
    ) : MomentsCommentViewEvent()

    data class UpdateCommentReaction(
        val position: Int,
        val reactionUpdate: ReactionUpdate
    ) : MomentsCommentViewEvent()

    data class UpdateCommentReactionMeera(
        val position: Int,
        val reactionUpdate: MeeraReactionUpdate
    ) : MomentsCommentViewEvent()
}
