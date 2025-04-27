package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.di.modules.APPLICATION_COROUTINE_SCOPE
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudePropertyCommentMenuAction
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.menu.CommentMenuItem
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.util.HandleCommentResponseUtil
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.comments.data.repository.SendCommentError
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.comments.domain.usecase.ComplainCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.ComplainCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.CopyCommentTextUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.DeletePostCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.DeletePostCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.SendCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.SendCommentUseCase
import com.numplates.nomera3.modules.comments.domain.usecase.ToGetCommentParams
import com.numplates.nomera3.modules.comments.domain.usecase.ToGetCommentsUseCase
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostParams
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.usecase.MeeraGetCommandReactionStreamUseCase
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import javax.inject.Named

const val COMMENTS_BOTTOM_SHEET_INVALID_COMMENT_ID = -1L

class CommentsBottomSheetViewModel @Inject constructor(
    private val getUserId: GetUserUidUseCase,
    private val toGetCommentUseCase: ToGetCommentsUseCase,
    private val sendCommentUseCase: SendCommentUseCase,
    private val copyCommentTextUseCase: CopyCommentTextUseCase,
    private val deletePostCommentUseCase: DeletePostCommentUseCase,
    private val complainComment: ComplainCommentUseCase,
    private val blockUser: BlockStatusUseCase,
    private val forceUpdatePostUseCase: ForceUpdatePostUseCase,
    private val getCommandReactionStreamUseCase: MeeraGetCommandReactionStreamUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val amplitudeComments: AmplitudeCommentsAnalytics,
    @Named(APPLICATION_COROUTINE_SCOPE)
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val _liveComments = MutableStateFlow<CommentChunk?>(null)
    val liveComments = _liveComments.asStateFlow()

    private val _commentEffect = MutableSharedFlow<CommentViewEffect>()
    val commentEffect = _commentEffect.asSharedFlow()

    var contentItem: BottomSheetCommentsInfoUiModel? = null

    private val commentList = mutableListOf<CommentUIType>()
    val commentObserver: (MutableList<CommentUIType>) -> Unit = { newCommentList ->
        commentList.clear()
        commentList.addAll(newCommentList)
    }

    val paginationHelper = PaginationHelper()
    private val toBeDeletedComments = CopyOnWriteArraySet<ToBeDeletedCommentEntity>()
    private val mapper = CommentsEntityResponseMapper(paginationHelper, toBeDeletedComments)

    private var blockedUsersList = mutableSetOf<Long>()

    private val handleCommentResponseUtil = HandleCommentResponseUtil(
        paginationHelper = paginationHelper,
        mapper = mapper,
        commentList = commentList,
        toBeDeletedComments = toBeDeletedComments
    ) { newCommentEvent ->
        viewModelScope.launch {
            _commentEffect.emit(newCommentEvent)
        }
    }

    init {
        observeReactions()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun init(
        contentItem: BottomSheetCommentsInfoUiModel,
        commentToOpenId: Long = COMMENTS_BOTTOM_SHEET_INVALID_COMMENT_ID,
    ) {
        this.contentItem = contentItem
        initialComments(commentToOpenId)
    }

    fun onCommentAction(action: CommentViewAction) {
        when (action) {
            is CommentViewAction.OpenMenuForComment -> openMenuForComment(action)
            is CommentViewAction.ReplyToComment -> onReplyToComment(action.comment, isFromMenu = false)
            is CommentViewAction.ReplyToCommentMenuAction -> onReplyToComment(action.comment, isFromMenu = true)
            is CommentViewAction.CopyMessageMenuAction -> onCopyMessageClicked(action)
            is CommentViewAction.MarkAsDeletedCommentMenuAction -> onMarkAsDeletedComment(action)
            is CommentViewAction.CancelDeleteComment -> onCancelDeleteComment(action)
            is CommentViewAction.DeleteComment -> onDeleteComment(action)
            is CommentViewAction.AddComplaintForCommentMenuAction -> onCommentComplaintAction(action)
            is CommentViewAction.BlockUserMenuAction -> onBlockUserAction(action)
            CommentViewAction.CommentMenuCancelAction -> onCancelMenuAction()
            CommentViewAction.AddCommentsAfter -> onAddCommentsAfter()
            CommentViewAction.AddCommentsBefore -> onAddCommentsBefore()
            is CommentViewAction.AddInnerComment -> onAddInnerComment(action)
            is CommentViewAction.SendCommentToServer -> onSendCommentToServer(action)
            else -> Unit
        }
    }

    fun getFeatureTogglesContainer() = featureTogglesContainer

    private fun observeReactions() {
        getCommandReactionStreamUseCase.execute().subscribe(::handleReactionUpdate).addTo(disposable)
    }

    private fun handleReactionUpdate(reactionUpdate: MeeraReactionUpdate) {
        when (reactionUpdate.reactionSource) {
            is MeeraReactionSource.CommentBottomSheet -> {
                val commentId = reactionUpdate.reactionSource.commentId
                val contentId = reactionUpdate.reactionSource.postId
                if (contentItem?.contentId != contentId) return

                val commentPosition = commentList.indexOfFirst { it.id == commentId }
                if (commentPosition == -1) return
                val comment = commentList[commentPosition] as? CommentEntity ?: return

                comment.comment.reactions = reactionUpdate.reactionList

                viewModelScope.launch {
                    _commentEffect.emit(CommentViewEffect.UpdateCommentReaction(commentPosition, reactionUpdate))
                }
            }
            else -> Unit
        }
    }

    private fun openMenuForComment(commentViewAction: CommentViewAction.OpenMenuForComment) {
        viewModelScope.launch {
            val userId = getUserId.invoke()
            val comment = commentViewAction.comment
            val commentAuthorId = comment.uid
            val commentId = comment.id
            val isCommentAuthor = commentAuthorId == userId
            val isContentAuthor = contentItem?.contentUserId == userId

            val menuItems = mutableListOf<CommentMenuItem>()

            menuItems.add(CommentMenuItem.ReplyToComment(comment))
            menuItems.add(CommentMenuItem.CopyMessage(comment))
            if (isCommentAuthor || isContentAuthor) {
                val whoDeleteComment = getWhoDeletedComment(isContentAuthor = isContentAuthor, isCommentAuthor = isCommentAuthor)
                menuItems.add(CommentMenuItem.DeleteComment(commentId, whoDeleteComment))
            }

            if (isCommentAuthor.not()) {
                menuItems.add(CommentMenuItem.AddComplaintForComment(commentId))
                if (isContentAuthor && !blockedUsersList.contains(commentAuthorId)) {
                    menuItems.add(CommentMenuItem.BlockUser(commentAuthorId))
                }
            }

            _commentEffect.emit(CommentViewEffect.OpenCommentMenu(menuItems))
        }
    }

    private fun getWhoDeletedComment(
        isContentAuthor: Boolean,
        isCommentAuthor: Boolean
    ): WhoDeleteComment? {
        return when {
            isContentAuthor && !isCommentAuthor -> WhoDeleteComment.POST_AUTHOR
            isContentAuthor && isCommentAuthor -> WhoDeleteComment.BOTH_POST_COMMENT_AUTHOR
            !isContentAuthor && isCommentAuthor -> WhoDeleteComment.COMMENT_AUTHOR
            else -> null
        }
    }

    private fun onReplyToComment(comment: CommentEntityResponse, isFromMenu: Boolean) {
        if (isFromMenu) logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPLY)
        viewModelScope.launch { _commentEffect.emit(CommentViewEffect.OnReplyToComment(comment)) }
    }

    private fun onCopyMessageClicked(action: CommentViewAction.CopyMessageMenuAction) {
        logCommentMenuAction(AmplitudePropertyCommentMenuAction.COPY)
        viewModelScope.launch {
            val success = copyCommentTextUseCase.invoke(action.comment)
            if (success) {
                _commentEffect.emit(CommentViewEffect.OnCopyCommentText)
            }
        }
    }

    private fun onMarkAsDeletedComment(action: CommentViewAction.MarkAsDeletedCommentMenuAction) {
        logCommentMenuAction(AmplitudePropertyCommentMenuAction.DELETE)
        viewModelScope.launch {
            val toBeDeleted = ToBeDeletedCommentEntity(
                id = action.originalComment.id,
                whoDeleteComment = action.whoDeleteComment,
                originalComment = action.originalComment
            )
            toBeDeletedComments.add(toBeDeleted)
            _commentEffect.emit(
                CommentViewEffect.MarkCommentForDeletion(
                    action.originalComment.id,
                    whoDeleteComment = action.whoDeleteComment
                )
            )
        }
    }

    private fun onCancelDeleteComment(action: CommentViewAction.CancelDeleteComment) {
        viewModelScope.launch {
            val toBeDeleted = toBeDeletedComments.find { it.id == action.originalComment.id }
            toBeDeletedComments.remove(toBeDeleted)
            _commentEffect.emit(
                CommentViewEffect.CancelDeleteComment(toBeDeleted?.originalComment ?: action.originalComment)
            )
        }
    }

    private fun onDeleteComment(action: CommentViewAction.DeleteComment) {
        applicationScope.launch {
            runCatching {
                deletePostCommentUseCase.invoke(DeletePostCommentParams((action.commentId)))
            }.onSuccess {
                updatePostCommentCounter()
                val toBeDeleted = toBeDeletedComments.find { deleted -> deleted.id == action.commentId }
                toBeDeletedComments.remove(toBeDeleted)
                _commentEffect.emit(CommentViewEffect.DeleteComment(action.commentId, action.whoDeleteComment))
                updatePostComments()
            }.onFailure {
                val toBeDeleted = toBeDeletedComments.find { deleted -> deleted.id == action.commentId }
                toBeDeletedComments.remove(toBeDeleted)
                _commentEffect.emit(
                    CommentViewEffect.ErrorDeleteComment(
                        toBeDeleted?.originalComment ?: action.originalComment
                    )
                )
                Timber.e(it)
            }
        }
    }

    private fun onCommentComplaintAction(action: CommentViewAction.AddComplaintForCommentMenuAction) {
        logCommentMenuAction(AmplitudePropertyCommentMenuAction.REPORT)
        viewModelScope.launch {
            val success = complainComment.execute(ComplainCommentParams(action.commentId))
            if (success) {
                _commentEffect.emit(CommentViewEffect.ComplainSuccess)
            } else {
                _commentEffect.emit(CommentViewEffect.NoInternet)
            }
        }
    }

    private fun onBlockUserAction(action: CommentViewAction.BlockUserMenuAction) {
        logCommentMenuAction(AmplitudePropertyCommentMenuAction.BLOCK)
        viewModelScope.launch(Dispatchers.IO) {
            val params = DefBlockParams(
                userId = getUserId.invoke(),
                remoteUserId = action.commentAuthorId,
                isBlocked = true
            )

            runCatching {
                blockUser.invoke(params)
            }.onSuccess {
                _commentEffect.emit(CommentViewEffect.OnAddUserToBlocked)
            }.onFailure {
                _commentEffect.emit(CommentViewEffect.NoInternet)
            }
        }
    }

    private fun onCancelMenuAction() {
        logCommentMenuAction(AmplitudePropertyCommentMenuAction.CANCEL)
    }

    private fun initialComments(commentToOpenId: Long) {
        val contentItem = contentItem ?: return
        paginationHelper.clear()
        paginationHelper.needToShowReplyBtn = contentItem.isCommentsEnabled
        paginationHelper.isLoadingAfter = true
        paginationHelper.isLoadingBefore = true

        viewModelScope.launch {
            val commentToScrollTo = commentToOpenId.takeIf { it > 0 }
            val params = ToGetCommentParams(postId = contentItem.contentId, commentId = commentToScrollTo)
            toGetCommentUseCase.execute(params, {
                viewModelScope.launch {
                    val commentChunk = mapper.map(it, OrderType.INITIALIZE).copy(scrollCommentId = commentToScrollTo)
                    _liveComments.emit(commentChunk)
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                    paginationHelper.isTopPage = true
                }
            }, {
                Timber.e(it)
                viewModelScope.launch {
                    _commentEffect.emit(CommentViewEffect.ShowTextError(null))

                    paginationHelper.isLastPage = true
                    paginationHelper.isLoadingAfter = false
                    paginationHelper.isLoadingBefore = false
                }
            })
        }
    }

    private fun onAddCommentsAfter() {
        viewModelScope.launch {
            val contentItem = contentItem ?: return@launch
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingAfterCallback(true)
            val params = ToGetCommentParams(
                contentItem.contentId, startId = paginationHelper.lastCommentId,
                order = OrderType.AFTER
            )

            toGetCommentUseCase.execute(params,
                {
                    viewModelScope.launch {
                        paginationHelper.isLoadingAfterCallback(false)
                        _liveComments.emit(mapper.map(it, OrderType.AFTER))
                        paginationHelper.isLoadingAfter = false
                    }
                },
                {
                    paginationHelper.isLoadingAfterCallback(false)
                    paginationHelper.isLastPage = true
                    paginationHelper.isLoadingAfter = false
                }
            )
        }
    }

    private fun onAddCommentsBefore() {
        viewModelScope.launch {
            val contentItem = contentItem ?: return@launch
            paginationHelper.isLoadingBefore = true
            paginationHelper.isLoadingBeforeCallback(true)
            val params = ToGetCommentParams(
                contentItem.contentId, startId = paginationHelper.firstCommentId,
                order = OrderType.BEFORE
            )
            toGetCommentUseCase.execute(params,
                {
                    viewModelScope.launch {
                        paginationHelper.isLoadingBeforeCallback(false)
                        _liveComments.emit(mapper.map(it, OrderType.BEFORE))
                        paginationHelper.isLoadingBefore = false
                    }
                },
                {
                    paginationHelper.isTopPage = true
                    paginationHelper.isLoadingBefore = false
                    paginationHelper.isLoadingBeforeCallback(false)
                }
            )
        }
    }

    private fun onAddInnerComment(action: CommentViewAction.AddInnerComment) {
        val contentItem = contentItem ?: return
        viewModelScope.launch {
            paginationHelper.isLoadingAfter = true
            paginationHelper.isLoadingBefore = true

            val params = with(action.separatorEntity.data) {
                ToGetCommentParams(
                    postId = contentItem.contentId,
                    startId = targetCommentId,
                    parentId = parentId,
                    order = orderType
                )
            }

            toGetCommentUseCase.execute(params,
                {
                    viewModelScope.launch {
                        val preparedData = mapper.mapInnerCommentsToChunk(
                            it,
                            action.separatorEntity.data.orderType,
                            action.separatorEntity
                        )
                        _liveComments.emit(preparedData)
                        paginationHelper.isLoadingAfter = false
                        paginationHelper.isLoadingBefore = false
                    }
                },
                {
                    viewModelScope.launch {
                        paginationHelper.isLoadingAfter = false
                        paginationHelper.isLoadingBefore = false
                        _commentEffect.emit(CommentViewEffect.ErrorInnerPagination(action.separatorEntity))
                    }
                }
            )
        }
    }

    private fun onSendCommentToServer(action: CommentViewAction.SendCommentToServer) {
        val contentItem = contentItem ?: return
        viewModelScope.launch {
            val res = sendCommentUseCase.execute(SendCommentParams(
                postId = contentItem.contentId,
                text = action.message,
                commentId = action.parentCommentId,
                errorTypeListener = { handleSendCommentError(it) }
            ))
            _commentEffect.emit(CommentViewEffect.EnableComments)
            val myComment = res?.myComment ?: return@launch
            val beforeMyComment = res.lastComments?.comments ?: listOf()

            if (myComment.parentId == null || myComment.parentId == 0L) {
                handleCommentResponseUtil.handleCommentResponseSuccess(
                    beforeMyComment = beforeMyComment,
                    afterMyComment = mutableListOf(),
                    myComment = myComment,
                    isSendingComment = true
                )
            } else {
                handleInnerCommentResponse(res, myComment.parentId)
            }
            updatePostCommentCounter()
            updatePostComments()
        }
    }

    private fun handleSendCommentError(commentError: SendCommentError) {
        when (commentError) {
            is SendCommentError.UnknownHost ->
                viewModelScope.launch { _commentEffect.emit(CommentViewEffect.NoInternetAction) }
            is SendCommentError.SendFail ->
                viewModelScope.launch { _commentEffect.emit(CommentViewEffect.ErrorPublishComment) }
            is SendCommentError.UserDeletedPostComment ->
                viewModelScope.launch {
                    _commentEffect.emit(CommentViewEffect.ShowTextError(commentError.messageError))
                }
        }
    }

    private fun handleInnerCommentResponse(res: SendCommentResponse, parentId: Long) {
        res.lastComments ?: return

        runCatching {
            val preparedData = mapper.mapInnerCommentsToChunk(old = res.lastComments, order = OrderType.BEFORE, parentId = parentId)

            viewModelScope.launch {
                _commentEffect.emit(
                    CommentViewEffect.NewInnerCommentSuccess(
                        chunk = preparedData,
                        parentId = parentId
                    )
                )
            }
        }
    }

    private fun updatePostCommentCounter() {
        contentItem?.let { item ->
            val normalCommentsAmount = commentList.count { it is CommentEntity }
            forceUpdatePostUseCase.execute(
                UpdatePostParams(
                    FeedUpdateEvent.FeedUpdatePayload(
                        postId = item.contentId,
                        commentCount = normalCommentsAmount
                    )
                )
            )
        }
    }

    private fun updatePostComments() {
        val contentItem = contentItem ?: return

        forceUpdatePostUseCase.execute(
            UpdatePostParams(
                FeedUpdateEvent.FeedUpdatePostComments(
                    postId = contentItem.contentId,
                    comments = commentList
                )
            )
        )
    }

    private fun logCommentMenuAction(action: AmplitudePropertyCommentMenuAction) {
        val commentItem = contentItem ?: return
        amplitudeComments.logCommentMenuAction(
            action = action,
            where = commentItem.commentsOrigin.toAmplitudePropertyWhere(),
            whence = commentItem.baseScreenOrigin.toAmplitudePropertyWhence()
        )
    }

}
