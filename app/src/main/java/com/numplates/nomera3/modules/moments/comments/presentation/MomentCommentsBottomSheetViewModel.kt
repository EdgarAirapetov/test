package com.numplates.nomera3.modules.moments.comments.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.comments.domain.usecase.ComplaintMomentCommentUseCase
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.entity.ToBeDeletedCommentEntity
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.moments.comments.domain.MomentCommentSendUseCase
import com.numplates.nomera3.modules.moments.comments.domain.MomentDeleteCommentUseCase
import com.numplates.nomera3.modules.moments.comments.domain.MomentGetCommentsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UpdateCommentCounterUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

class MomentCommentsBottomSheetViewModel @Inject constructor(
    private val getCommentsUseCase: MomentGetCommentsUseCase,
    private val sendCommentUseCase: MomentCommentSendUseCase,
    private val complainMomentCommentUseCase: ComplaintMomentCommentUseCase,
    private val updateCommentCounterUseCase: UpdateCommentCounterUseCase,
    private val deleteCommentUseCase: MomentDeleteCommentUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val blockUserUseCase: BlockStatusUseCase,
    private val reactionRepository: ReactionRepository,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val amplitudeComments: AmplitudeCommentsAnalytics
) : ViewModel() {

    private var momentItem: MomentItemUiModel? = null
    private var commentToOpenId: Long? = null

    private val viewEvent = SingleLiveEvent<MomentsCommentViewEvent>()
    private val commentList = mutableListOf<CommentUIType>()
    private val paginationHelper = PaginationHelper()

    private val toBeDeletedComments = CopyOnWriteArraySet<ToBeDeletedCommentEntity>()
    private val mapper = CommentsEntityResponseMapper(paginationHelper, toBeDeletedComments)

    private val handleCommentResponseUtil = HandleMomentCommentResponseUtil(
        paginationHelper = paginationHelper,
        mapper = mapper,
        commentList = commentList,
        viewEvent = viewEvent,
        toBeDeletedComments = toBeDeletedComments
    )

    val commentTreeModelController =
        MomentCommentTreeModel(
            viewModelScope = viewModelScope,
            getCommentsUseCase = getCommentsUseCase,
            commentList = commentList,
            mapper = mapper,
            paginationHelper = paginationHelper,
            viewEvent = viewEvent
        )

    val commentCreateModelController =
        MomentCommentCreateModel(
            viewEvent = viewEvent,
            viewModelScope = viewModelScope,
            sendCommentUseCase = sendCommentUseCase,
            handleCommentResponseUtil = handleCommentResponseUtil,
            getUserUidUseCase = getUserUidUseCase,
            amplitudeComments = amplitudeComments,
            mapper = mapper
        )

    val commentReactionModelController =
        MomentCommentReactionModel(
            reactionRepository = reactionRepository,
            commentList = commentList,
            viewEvent = viewEvent
        )

    val meeraCommentReactionModelController =
        MeeraMomentCommentReactionModel(
            reactionRepository = reactionRepository,
            commentList = commentList,
            viewEvent = viewEvent
        )

    val commentBottomMenuController =
        MomentCommentMenuModel(
            paginationHelper = paginationHelper,
            toBeDeletedComments = toBeDeletedComments,
            viewEvent = viewEvent,
            viewModelScope = viewModelScope,
            complainMomentComment = complainMomentCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            updateCommentCounterUseCase = updateCommentCounterUseCase,
            blockUser = blockUserUseCase
        )

    override fun onCleared() {
        super.onCleared()
        commentReactionModelController.onCleared()
        meeraCommentReactionModelController.onCleared()
    }

    fun getFeatureToggleContainer(): FeatureTogglesContainer {
        return featureTogglesContainer
    }

    fun getViewEvent(): LiveData<MomentsCommentViewEvent> {
        return viewEvent
    }

    fun init(momentItem: MomentItemUiModel, commentToOpenId: Long? = null) {
        this.momentItem = momentItem
        this.commentToOpenId = commentToOpenId

        commentTreeModelController.init(momentItem, commentToOpenId)
        commentCreateModelController.init(momentItem)
    }

    fun handleDismiss() {
        fetchAndUpdateMomentItem()
    }

    private fun fetchAndUpdateMomentItem() {
        val momentItemId = momentItem?.id ?: return

        viewModelScope.launch {
            runCatching {
                updateCommentCounterUseCase.invoke(momentItemId)
            }
        }
    }
}
