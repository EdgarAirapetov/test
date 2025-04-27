package com.numplates.nomera3.modules.moments.comments.presentation

import androidx.lifecycle.MutableLiveData
import com.meera.core.network.MeraNetworkException
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.NetworkState
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCommentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.comments.AmplitudeCommentsAnalytics
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.comments.domain.mapper.CommentsEntityResponseMapper
import com.numplates.nomera3.modules.moments.comments.domain.MomentCommentSendUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MomentCommentCreateModel(
    private val viewEvent: MutableLiveData<MomentsCommentViewEvent>,
    private val viewModelScope: CoroutineScope,
    private val sendCommentUseCase: MomentCommentSendUseCase,
    private val mapper: CommentsEntityResponseMapper,
    private val handleCommentResponseUtil: HandleMomentCommentResponseUtil,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudeComments: AmplitudeCommentsAnalytics
) {
    var loadingStatusLive: MutableLiveData<NetworkState.Status> = MutableLiveData()

    private var momentItem: MomentItemUiModel? = null

    fun init(momentItem: MomentItemUiModel) {
        this.momentItem = momentItem
    }

    fun sendCommentToServer(
        moment: MomentItemUiModel, //used for analytycs
        message: String,
        parentCommentId: Long
    ) {
        val momentItemId = momentItem?.id ?: return

        loadingStatusLive.value = NetworkState.Status.RUNNING
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                sendCommentUseCase.invoke(
                    momentItemId = momentItemId,
                    text = message,
                    commentId = parentCommentId,
                )
            }.onFailure { exception ->
                Timber.e(exception)
                when (exception) {
                    is MomentCommentSendUseCase.CommentRestrictedException -> {
                        viewEvent.postValue(MomentsCommentViewEvent.CommentRestricted(exception.userMessage))
                    }
                    is MeraNetworkException -> {
                        viewEvent.postValue(MomentsCommentViewEvent.NoInternetAction)
                    }
                    else -> {
                        viewEvent.postValue(MomentsCommentViewEvent.ErrorPublishMomentComment)
                    }
                }
            }.onSuccess { res ->
                launch(Dispatchers.Main) {
                    val data = res.data

                    viewEvent.value = MomentsCommentViewEvent.EnableComments
                    val myComment = data.myComment ?: return@launch
                    val beforeMyComment = data.lastComments?.comments ?: listOf()

                    if (myComment.parentId == null || myComment.parentId == 0L) {
                        handleCommentResponseUtil.handleCommentResponseSuccess(
                            beforeMyComment = beforeMyComment,
                            afterMyComment = mutableListOf(),
                            myComment = myComment,
                            isSendingComment = true
                        )
                    } else {
                        handleInnerCommentResponse(data, myComment.parentId)
                    }
                }

            }
        }
        logCommentSent(moment, parentCommentId)
    }

    private fun logCommentSent(moment: MomentItemUiModel, parentCommentId: Long) {
        val commentType = if (parentCommentId == 0L) {
            AmplitudePropertyCommentType.COMMENT
        } else {
            AmplitudePropertyCommentType.REPLAY
        }

        amplitudeComments.logSentComment(
            postId = 0,
            authorId = moment.userId,
            commentorId = getUserUidUseCase.invoke(),
            momentId = moment.id,
            commentType = commentType,
            postType = AmplitudePropertyPostType.NONE,
            postContentType = AmplitudePropertyContentType.SINGLE,
            whence = AmplitudePropertyWhence.OTHER,
            where = AmplitudePropertyWhere.MOMENT,
            recFeed = false
        )
    }

    private fun handleInnerCommentResponse(res: SendCommentResponse, parentId: Long) {
        res.lastComments ?: return

        val preparedData = mapper.mapInnerCommentsToChunk(old = res.lastComments, order = OrderType.BEFORE, parentId = parentId)

        viewEvent.postValue(
            MomentsCommentViewEvent.NewInnerCommentSuccess(
                chunk = preparedData,
                parentId = parentId
            )
        )
    }
}
