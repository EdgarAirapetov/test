package com.numplates.nomera3.modules.upload.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.db.models.UploadType
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.IsUserAuthorizedUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.DownloadVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetAllDownloadingMediaEventUseCase
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetAvailableMapEventCountUseCase
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.upload.domain.UploadState
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.upload.ui.ProcessingQueue
import com.numplates.nomera3.modules.upload.ui.mapper.UploadUiMapper
import com.numplates.nomera3.modules.upload.ui.model.StatusToastAction
import com.numplates.nomera3.modules.upload.ui.model.StatusToastEvent
import com.numplates.nomera3.modules.upload.ui.model.StatusToastUiModel
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MEDIA_DOWNLOAD_SHOW_SUCCESS_TOAST_DELAY = 1000L

/**
 * Shared viewModel for upload
 * */
class UploadStatusViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val getAvailableMapEventCountUseCase: GetAvailableMapEventCountUseCase,
    private val uploadUiMapper: UploadUiMapper,
    private val startDownloadingVideoToGalleryUseCase: DownloadVideoToGalleryUseCase,
    private val getAllDownloadingVideoWorkInfosUseCase: GetAllDownloadingMediaEventUseCase,
    private val postsRepository: PostsRepository,
    private val isUserAuthorizedUseCase: IsUserAuthorizedUseCase
) : ViewModel() {

    private val _statusToastActionFlow = MutableSharedFlow<StatusToastAction>()
    val statusToastActionFlow = _statusToastActionFlow.asSharedFlow()

    private val _statusToastEventFlow = MutableSharedFlow<StatusToastEvent>()
    val statusToastEventFlow = _statusToastEventFlow.asSharedFlow()

    private val startTime = System.currentTimeMillis()
    private var lastToastStatusUiModel: StatusToastUiModel? = null
    private val processingQueue = ProcessingQueue<StatusToastUiModel>(
        onPayloadEnter = { payload ->
            lastToastStatusUiModel = payload
            viewModelScope.launch {
                emitStatusToastEventWithAuthCheck(StatusToastEvent.ShowToast(payload))
            }
        },
        onPayloadExit = { payload, forced ->
            viewModelScope.launch {
                _statusToastEventFlow.emit(StatusToastEvent.HideToast)
            }
        }
    )

    init {
        uploadRepository.getState()
            .onEach(this::onState)
            .launchIn(viewModelScope)

        observeDownloadingVideoWorkInfos()
    }

    fun restoreStatusToast() {
        val lastStatus = lastToastStatusUiModel ?: return
        viewModelScope.launch {
            emitStatusToastEventWithAuthCheck(StatusToastEvent.ShowToast(lastStatus))
        }
    }

    fun hideStatusToast() {
        if (isStartTimePassed()) {
            processingQueue.clear()
        }
    }

    fun onStatusAction(action: StatusToastAction) {
        processingQueue.forceExit()
        when (action) {
            StatusToastAction.RetryUpload -> {
                uploadRepository.retryLastFailed()
            }
        }
        viewModelScope.launch {
            _statusToastActionFlow.emit(action)
        }
    }

    fun retryPostMediaDownload(postId: Long, assetId: String?) {
        val downloadMediaType = DownloadMediaHelper.PostMediaDownloadType.PostToastRetryDownload(postId, assetId)
        startDownloadingVideoToGalleryUseCase.invoke(downloadMediaType)
    }

    fun onToastDismiss() {
        processingQueue.forceExit()
    }

    fun abortEditingPost(postId: Long) {
        viewModelScope.launch {
            postsRepository.newPostObservable.onNext(PostActionModel.PostEditingAbortModel(postId = postId))
        }
    }

    private fun observeDownloadingVideoWorkInfos() {
        viewModelScope.launch {
            getAllDownloadingVideoWorkInfosUseCase.invoke().collect { downloadEvent ->
                handleDownloadEvent(downloadEvent)
            }
        }
    }

    private fun handleDownloadEvent(event: DownloadMediaEvent) {
        when (event) {
            is DownloadMediaEvent.PostAlreadyDownloading -> {
                handleAlreadyDownloading()
            }
            is DownloadMediaEvent.PostDownloadState -> {
                handleVideoLoading(event.postMediaDownloadType, event.state)
            }
        }
    }

    private fun handleAlreadyDownloading() {
        viewModelScope.launch {
            emitStatusToastEventWithAuthCheck(
                StatusToastEvent.ShowBottomToast(
                    message = R.string.media_already_downloading,
                    isError = true
                )
            )
        }
    }

    private fun handleVideoLoading(
        postMediaDownloadType: DownloadMediaHelper.PostMediaDownloadType,
        mediaLoadingState: MediaLoadingState
    ) {
        when (mediaLoadingState) {
            MediaLoadingState.SUCCESS -> {
                viewModelScope.launch {
                    delay(MEDIA_DOWNLOAD_SHOW_SUCCESS_TOAST_DELAY)
                    emitStatusToastEventWithAuthCheck(
                        StatusToastEvent.ShowMediaDownloadSuccessToast(
                            postMediaDownloadType
                        )
                    )
                }
            }
            MediaLoadingState.FAIL -> {
                viewModelScope.launch {
                    emitStatusToastEventWithAuthCheck(
                        StatusToastEvent.ShowMediaDownloadErrorBottomToast(
                            postId = postMediaDownloadType.postId,
                            assetId = postMediaDownloadType.assetId
                        )
                    )
                }
            }
            else -> Unit
        }
    }

    private fun isStartTimePassed(): Boolean {
        return System.currentTimeMillis() - startTime > SCREEN_OPEN_TOAST_HIDE_FORBIDDEN_DELAY_MS
    }

    private suspend fun onState(uploadState: UploadState) {
        if ((uploadState.status as? UploadStatus.Failed)?.maxTriesReached == true) {
            processingQueue.clear()
            viewModelScope.launch {
                emitStatusToastEventWithAuthCheck(StatusToastEvent.ShowUploadError(uploadState.uploadItem))
            }
        } else {
            val availableEventCount = if (
                uploadState.status is UploadStatus.Success
                && uploadState.uploadItem.type == UploadType.EventPost
            ) {
                runCatching {
                    getAvailableMapEventCountUseCase.invoke()
                }.getOrDefault(null)
            } else {
                null
            }
            val queueItem = uploadUiMapper.mapUploadItem(
                uploadState = uploadState,
                availableEventCount = availableEventCount
            )
            processingQueue.postItem(queueItem)
        }
    }

    private suspend fun emitStatusToastEventWithAuthCheck(event: StatusToastEvent) {
        if (isUserAuthorizedUseCase.invoke()) {
            _statusToastEventFlow.emit(event)
        } else {
            _statusToastEventFlow.emit(StatusToastEvent.HideToast)
        }
    }

    companion object {
        private const val SCREEN_OPEN_TOAST_HIDE_FORBIDDEN_DELAY_MS = 5000L
    }
}
