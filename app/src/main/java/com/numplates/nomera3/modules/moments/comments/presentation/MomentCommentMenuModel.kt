package com.numplates.nomera3.modules.moments.comments.presentation

import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.modules.comments.domain.usecase.ComplainCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.ComplaintMomentCommentUseCase
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.moments.comments.domain.MomentDeleteCommentUseCase
import com.numplates.nomera3.modules.moments.show.domain.UpdateCommentCounterUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MomentCommentMenuModel(
    val paginationHelper: PaginationHelper,
    private val toBeDeletedComments: MutableSet<ToBeDeletedCommentEntity>,
    private val viewEvent: MutableLiveData<MomentsCommentViewEvent>,
    private val viewModelScope: CoroutineScope,
    private val complainMomentComment: ComplaintMomentCommentUseCase,
    private val deleteCommentUseCase: MomentDeleteCommentUseCase,
    private val updateCommentCounterUseCase: UpdateCommentCounterUseCase,
    private val blockUser: BlockStatusUseCase
) {
    fun complainComment(commentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = complainMomentComment.execute(ComplainCommentParams(commentId))
            if (success) {
                viewEvent.postValue(MomentsCommentViewEvent.ComplainSuccess)
            } else {
                viewEvent.postValue(MomentsCommentViewEvent.NoInternet)
            }
        }
    }

    fun blockUser(userId: Long, remoteUserId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val params = DefBlockParams(
                userId = userId,
                remoteUserId = remoteUserId,
                isBlocked = true
            )

            kotlin.runCatching {
                blockUser.invoke(params)
            }.onSuccess {
                viewEvent.postValue(MomentsCommentViewEvent.OnAddUserToBlocked(remoteUserId))
            }.onFailure {
                viewEvent.postValue(MomentsCommentViewEvent.NoInternet)
            }
        }
    }

    fun markAsDeleteComment(
        originalComment: CommentUIType,
        whoDeleteComment: WhoDeleteComment
    ) {
        val toBeDeleted = ToBeDeletedCommentEntity(
            id = originalComment.id,
            whoDeleteComment = whoDeleteComment,
            originalComment = originalComment
        )
        toBeDeletedComments.add(toBeDeleted)
        viewEvent.postValue(
            MomentsCommentViewEvent.MarkCommentForDeletion(
                commentID = originalComment.id,
                whoDeleteComment = whoDeleteComment
            )
        )
    }

    fun cancelDeleteComment(
        originalComment: CommentUIType
    ) {
        val toBeDeleted = toBeDeletedComments.find { it.id == originalComment.id }
        toBeDeletedComments.remove(toBeDeleted)
        viewEvent.postValue(
            MomentsCommentViewEvent.CancelDeleteComment(
                toBeDeleted?.originalComment ?: originalComment
            )
        )
    }

    fun deleteComment(
        momentItemId: Long,
        comment: CommentUIType,
        whoDeleteComment: WhoDeleteComment
    ) {
        GlobalScope.launch {
            kotlin.runCatching {
                deleteCommentUseCase.invoke(comment.id)
                reFetchMomentItem(momentItemId)
            }.onSuccess {
                toBeDeletedComments.removeIf { deleted -> deleted.id == comment.id }
                viewEvent.postValue(
                    MomentsCommentViewEvent.DeleteComment(
                        commentID = comment.id,
                        whoDeleteComment = whoDeleteComment
                    )
                )
            }.onFailure { exception ->
                val toBeDeleted = toBeDeletedComments.find { deleted -> deleted.id == comment.id }
                toBeDeletedComments.remove(toBeDeleted)
                Timber.e(exception)
                viewEvent.postValue(MomentsCommentViewEvent.ErrorDeleteComment(toBeDeleted?.originalComment ?: comment))
            }
        }
    }

    private fun reFetchMomentItem(momentItemId: Long) {
        kotlin.runCatching {
            updateCommentCounterUseCase.invoke(momentItemId)
        }.onFailure { exception ->
            Timber.e(exception)
        }
    }
}
