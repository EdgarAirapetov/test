package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import com.numplates.nomera3.modules.comments.bottomsheet.presentation.menu.CommentMenuItem
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate

sealed class CommentViewEffect {
    object EnableComments : CommentViewEffect()
    object OnScrollToBottom : CommentViewEffect()

    object ComplainSuccess : CommentViewEffect()

    data class OpenCommentMenu(val menuItems: List<CommentMenuItem>) : CommentViewEffect()
    data class OnReplyToComment(val comment: CommentEntityResponse) : CommentViewEffect()
    object OnCopyCommentText : CommentViewEffect()

    object OnAddUserToBlocked : CommentViewEffect()

    class MarkCommentForDeletion(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : CommentViewEffect()

    class CancelDeleteComment(
        val originalComment: CommentUIType
    ) : CommentViewEffect()

    class DeleteComment(
        val commentID: Long,
        val whoDeleteComment: WhoDeleteComment
    ) : CommentViewEffect()

    data class ErrorInnerPagination(
        val data: CommentSeparatorEntity
    ) : CommentViewEffect()

    data class NewCommentSuccess(
        var myCommentId: Long,
        var beforeMyComment: List<CommentUIType>,
        var afterMyComment: List<CommentUIType>,
        var hasIntersection: Boolean,
        var needSmoothScroll: Boolean,
        var needToShowLastFullComment: Boolean
    ) : CommentViewEffect()

    data class NewInnerCommentSuccess(
        val parentId: Long,
        val chunk: CommentChunk
    ) : CommentViewEffect()

    class ErrorDeleteComment(
        val comment: CommentUIType
    ) : CommentViewEffect()

    object NoInternet : CommentViewEffect()
    object NoInternetAction : CommentViewEffect()

    object ErrorPublishComment : CommentViewEffect()

    object CommentRestricted : CommentViewEffect()

    data class ShowTextError(
        val message: String?
    ) : CommentViewEffect()

    data class UpdateCommentReaction(
        val position: Int,
        val reactionUpdate: MeeraReactionUpdate
    ) : CommentViewEffect()
}
