package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment

sealed interface CommentViewAction {

    data class OpenMenuForComment(val comment: CommentEntityResponse) : CommentViewAction

    object AddCommentsBefore : CommentViewAction
    object AddCommentsAfter : CommentViewAction
    data class AddInnerComment(val separatorEntity: CommentSeparatorEntity) : CommentViewAction
    data class SendCommentToServer(val message: String, val parentCommentId: Long) : CommentViewAction
    data class CancelDeleteComment(val originalComment: CommentUIType) : CommentViewAction
    data class DeleteComment(val commentId: Long, val originalComment: CommentUIType, val whoDeleteComment: WhoDeleteComment) : CommentViewAction

    data class ReplyToComment(val comment: CommentEntityResponse) : CommentViewAction

    data class ReplyToCommentMenuAction(val comment: CommentEntityResponse) : CommentViewAction
    data class CopyMessageMenuAction(val comment: CommentEntityResponse) : CommentViewAction
    data class MarkAsDeletedCommentMenuAction(val originalComment: CommentUIType, val whoDeleteComment: WhoDeleteComment) : CommentViewAction
    data class AddComplaintForCommentMenuAction(val commentId: Long) : CommentViewAction
    data class BlockUserMenuAction(val commentAuthorId: Long) : CommentViewAction
    object CommentMenuCancelAction : CommentViewAction

}
