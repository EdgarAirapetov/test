package com.numplates.nomera3.modules.comments.bottomsheet.presentation.util

import com.numplates.nomera3.modules.comments.bottomsheet.presentation.CommentViewEffect
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.DeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.comments.ui.util.checkHasCommentIntersection

/**
 * Класс для инкапсуляции метода handleCommentResponse (чтобы не засорять ViewModel)
 */
class HandleCommentResponseUtil(
    private val paginationHelper: PaginationHelper,
    private val mapper: CommentsEntityResponseMapper,
    private val commentList: List<CommentUIType>,
    private val toBeDeletedComments: Set<ToBeDeletedCommentEntity>,
    private val newCommentEvent: (CommentViewEffect.NewCommentSuccess) -> Unit
) {
    fun handleCommentResponseSuccess(
        beforeMyComment: List<CommentEntityResponse>,
        afterMyComment: List<CommentEntityResponse>,
        myComment: CommentEntityResponse,
        needSmoothScroll: Boolean = true,
        isSendingComment: Boolean = false
    ) {
        val lastCommentId = getLastValidCommentId()
        val hasIntersections = lastCommentId?.checkHasCommentIntersection(beforeMyComment) ?: false
        val beforeNew = mutableListOf<CommentEntityResponse>()
        if (!hasIntersections) paginationHelper.clear()
        beforeNew.addAll(beforeMyComment.filter { item ->
            item.id > (lastCommentId ?: Long.MIN_VALUE)
        })

        newCommentEvent.invoke(
            CommentViewEffect.NewCommentSuccess(
                myCommentId = myComment.id,
                beforeMyComment = mapper.mapCommentsForNewMessage(
                    oldList = beforeNew,
                    order = OrderType.BEFORE,
                    hasIntersection = hasIntersections
                ),
                afterMyComment = mapper.mapCommentsForNewMessage(
                    oldList = afterMyComment,
                    order = OrderType.AFTER,
                    hasIntersection = hasIntersections
                ),
                hasIntersection = hasIntersections,
                needSmoothScroll = needSmoothScroll,
                needToShowLastFullComment = isSendingComment
            )
        )

        paginationHelper.isTopPage = false
        paginationHelper.isLastPage = false
    }

    private fun isCommentHasChild(commentId: Long): Boolean {
        val child = commentList.findLast { it.parentId == commentId }
        return child != null
    }


    private fun getLastValidCommentId(): Long? {
        for (i in commentList.size - 1 downTo 0) {
            val comment = commentList[i]
            val hasParent = comment.parentId != null
            val isToBeDeleted = toBeDeletedComments.any { it.id == comment.id }
            val isDeleted = comment is DeletedCommentEntity
            val hasChild = isCommentHasChild(comment.id)
            val isValidDeleted = hasChild || isToBeDeleted
            if ((!hasParent && !isDeleted) || (isDeleted && isValidDeleted)) {
                return comment.id
            }
        }
        return paginationHelper.lastCommentId
    }
}
