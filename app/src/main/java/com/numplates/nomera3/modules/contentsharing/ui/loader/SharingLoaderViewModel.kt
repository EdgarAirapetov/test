package com.numplates.nomera3.modules.contentsharing.ui.loader

import androidx.lifecycle.viewModelScope
import com.meera.core.base.viewmodel.BaseViewModel
import com.numplates.nomera3.modules.contentsharing.ui.ContentSharingAction
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingDataCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DELAY_BEFORE_CLOSING = 2000L
private const val EMPTY_PROGRESS = 0f
private const val FULL_PROGRESS = 1f

class SharingLoaderViewModel @Inject constructor(
    private val messageFilesUploader: MessageFilesUploader,
    private val messageContentManager: MessageContentManager,
    private val hapticManager: HapticManager,
) : BaseViewModel<SharingLoaderState, SharingLoaderEffect, SharingLoaderAction>() {

    private val sharingDataCache: SharingDataCache = SharingDataCache
    private val stateValue: SharingLoaderState
        get() = _state.value ?: SharingLoaderState()

    private var uploadJob: Job? = null

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun handleUIAction(action: SharingLoaderAction) {
        when (action) {
            is SharingLoaderAction.SendSharingData -> {
                sendSharingData()
            }

            is SharingLoaderAction.CancelDataUploading -> {
                cancelDataUploading()
            }

            else -> error("Please use correct action. Required type: ${ContentSharingAction::class.simpleName}")
        }
    }

    private fun sendSharingData() {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            if (sharingDataCache.contentLink != null) {
                sendLinkAndMessages()
            } else if (sharingDataCache.getUris().isNotEmpty()) {
                uploadDataAndSendMessages()
            } else {
                error("There is no data to send to chats.")
            }
        }
    }

    private fun cancelDataUploading() {
        uploadJob?.cancel()
        uploadJob = null
        viewModelScope.launch {
            completeCurrentState()
        }
    }

    private suspend fun sendLinkAndMessages() {
        emitState(stateValue.copy(isLoading = true, progress = EMPTY_PROGRESS))
        runCatching {
            messageContentManager.sendMessages(
                content = sharingDataCache.messageComment,
                link = requireNotNull(sharingDataCache.contentLink)
            )
        }.onFailure {
            completeOnFailure()
        }.onSuccess {
            completeSuccessfully()
        }
    }

    private suspend fun uploadDataAndSendMessages() {
        emitState(stateValue.copy(isLoading = true, progress = EMPTY_PROGRESS))
        runCatching {
            val attachments = messageFilesUploader.uploadMediaFiles(
                uris = sharingDataCache.getUris(),
                progress = { progress -> emitState(stateValue.copy(isLoading = true, progress = progress)) }
            )
            messageContentManager.sendMessages(
                content = sharingDataCache.messageComment,
                attachments = attachments
            )
        }.onFailure {
            completeOnFailure()
        }.onSuccess {
            completeSuccessfully()
        }
    }

    private suspend fun completeSuccessfully() {
        hapticManager.pushHaptic()
        emitState(stateValue.copy(isLoading = false, progress = FULL_PROGRESS))
        delay(DELAY_BEFORE_CLOSING)
        emitEffect(SharingLoaderEffect.FinishLoading)
    }

    private suspend fun completeOnFailure() {
        emitState(stateValue.copy(isLoading = false))
        emitEffect(SharingLoaderEffect.ShowWentWrongAlert)
    }

    private suspend fun completeCurrentState() {
        emitState(stateValue.copy(isLoading = false))
        emitEffect(SharingLoaderEffect.FinishLoading)
    }
}
