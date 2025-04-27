package com.numplates.nomera3.modules.moments.comments.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.moments.comments.domain.MomentGetCommentsUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MomentCommentTreeModel(
    private val viewEvent: SingleLiveEvent<MomentsCommentViewEvent>,
    private val viewModelScope: CoroutineScope,
    private val getCommentsUseCase: MomentGetCommentsUseCase,
    private val commentList: MutableList<CommentUIType>,
    private val mapper: CommentsEntityResponseMapper,
    val paginationHelper: PaginationHelper,
) {
    private var momentItem: MomentItemUiModel? = null
    private var commentToOpenId:Long? = null
    private val liveComments = MutableLiveData<CommentChunk>()

    val commentObserver: (MutableList<CommentUIType>) -> Unit = { newCommentList ->
        commentList.clear()
        commentList.addAll(newCommentList)
    }

    fun getIsMomentCommentable(): Boolean {
        val momentItem = momentItem ?: return false

        return momentItem.isMomentCommentable()
    }

    fun getLiveComments(): LiveData<CommentChunk> {
        return liveComments
    }

    fun getMomentItem(): MomentItemUiModel? {
        return momentItem
    }

    fun init(momentItem: MomentItemUiModel, commentToOpenId:Long?) {
        this.momentItem = momentItem
        this.commentToOpenId = commentToOpenId

        initialComments()
    }

    fun addInnerComment(commentSeparator: CommentSeparatorEntity) {
        val momentId = momentItem?.id ?: return
        val startId = commentSeparator.data.targetCommentId

        viewModelScope.launch {
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingBefore = true

            kotlin.runCatching {
                getCommentsUseCase.invoke(
                    momentItemId = momentId,
                    parentId = commentSeparator.parentId,
                    startId = startId,
                    order = commentSeparator.data.orderType
                )
            }.onSuccess { data ->
                val preparedData =
                    mapper.mapInnerCommentsToChunk(old = data, order = commentSeparator.data.orderType, separator = commentSeparator)
                liveComments.value = preparedData
                paginationHelper.isLoadingAfter = false
                paginationHelper.isLoadingBefore = false
            }.onFailure { exception ->
                Timber.e(exception)
                handleGetMomentsException(exception)

                paginationHelper.isLoadingAfter = false
                paginationHelper.isLoadingBefore = false
                viewEvent.postValue(MomentsCommentViewEvent.ErrorInnerPagination(commentSeparator))
            }
        }
    }

    fun addCommentsAfter() {
        val momentId = momentItem?.id ?: return

        viewModelScope.launch {
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingAfterCallback(true)

            kotlin.runCatching {
                getCommentsUseCase.invoke(
                    momentItemId = momentId,
                    startId = paginationHelper.lastCommentId,
                    order = OrderType.AFTER
                )
            }.onSuccess { data ->
                paginationHelper.isLoadingAfterCallback(false)
                liveComments.value = mapper.map(data, OrderType.AFTER)
                paginationHelper.isLoadingAfter = false
            }.onFailure { exception ->
                Timber.e(exception)
                handleGetMomentsException(exception)

                paginationHelper.isLoadingAfterCallback(false)
                paginationHelper.isLastPage = true
                paginationHelper.isLoadingAfter = false
            }
        }
    }

    fun addCommentsBefore() {
        val momentId = momentItem?.id ?: return

        viewModelScope.launch {
            paginationHelper.isLoadingBefore = true
            paginationHelper.isLoadingBeforeCallback(true)

            kotlin.runCatching {
                getCommentsUseCase.invoke(
                    momentItemId = momentId,
                    startId = paginationHelper.firstCommentId,
                    order = OrderType.BEFORE
                )
            }.onSuccess { data ->
                paginationHelper.isLoadingBeforeCallback(false)
                liveComments.value = mapper.map(data, OrderType.BEFORE)
                paginationHelper.isLoadingBefore = false
            }.onFailure { exception ->
                Timber.e(exception)
                handleGetMomentsException(exception)

                paginationHelper.isTopPage = true
                paginationHelper.isLoadingBefore = false
                paginationHelper.isLoadingBeforeCallback(false)
            }
        }
    }

    private fun handleGetMomentsException(exception: Throwable) {
        when (exception) {
            is MomentGetCommentsUseCase.MomentAccessRestrictedException -> {
                viewEvent.postValue(MomentsCommentViewEvent.CommentRestricted())
            }
            else -> {
                viewEvent.postValue(MomentsCommentViewEvent.ShowTextError(null))
            }
        }
    }

    private fun initialComments() {
        val momentItem = momentItem ?: return

        paginationHelper.clear()
        paginationHelper.needToShowReplyBtn = momentItem.isMomentCommentable()
        paginationHelper.isLoadingAfter = true
        paginationHelper.isLoadingBefore = true

        viewModelScope.launch {
            kotlin.runCatching {
                getCommentsUseCase.invoke(momentItemId = momentItem.id, commentId = commentToOpenId)
            }.onSuccess { data ->
                liveComments.value = mapper.map(data, OrderType.INITIALIZE).copy(scrollCommentId = commentToOpenId)
                paginationHelper.isLoadingAfter = false
                paginationHelper.isLoadingBefore = false
                paginationHelper.isTopPage = true
            }.onFailure { exception ->
                Timber.e(exception)
                handleGetMomentsException(exception)

                paginationHelper.isLastPage = true
                paginationHelper.isLoadingAfter = false
                paginationHelper.isLoadingBefore = false
            }
        }
    }
}
