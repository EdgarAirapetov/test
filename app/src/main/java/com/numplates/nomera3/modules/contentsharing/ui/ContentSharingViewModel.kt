package com.numplates.nomera3.modules.contentsharing.ui

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.meera.core.base.viewmodel.BaseViewModel
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingDataCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ALERT_DURATION = 2750L

class ContentSharingViewModel @Inject constructor() :
    BaseViewModel<ContentSharingState, ContentSharingEffect, ContentSharingAction>() {

    private val sharingDataCache: SharingDataCache = SharingDataCache
    private var sharingState: SharingState = SharingState.IDLE

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun handleUIAction(action: ContentSharingAction) {
        viewModelScope.launch {
            when (action) {
                is ContentSharingAction.ScheduleLoadingUri -> cacheUris(action.uris)
                is ContentSharingAction.ScheduleSendLink -> cacheLink(action.link)
                is ContentSharingAction.UpdateSharingState -> updateSharingState(action.sharingState)
                is ContentSharingAction.CloseWithAnError -> completeOnFailure()
                is ContentSharingAction.CloseWithoutAnError -> completeNoError()
                is ContentSharingAction.ShowNetworkError -> showNetworkAlertError()
                is ContentSharingAction.ShowVideoDurationError -> showVideoDurationError()
                is ContentSharingAction.CheckSharingState -> checkDismissState()
                else -> error("Please use correct action. Required type: ${ContentSharingAction::class.simpleName}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharingDataCache.clearData()
    }

    private fun updateSharingState(sharingState: SharingState) {
        this.sharingState = sharingState
    }

    private suspend fun checkDismissState() {
        when (sharingState) {
            SharingState.IDLE -> completeNoError()
            SharingState.PROGRESS -> Unit
        }
    }

    private suspend fun cacheUris(uris: List<Uri>) {
        sharingDataCache.cacheUris(uris)
        emitEffect(ContentSharingEffect.SelectChatsToUpload)
    }

    private suspend fun cacheLink(link: String) {
        sharingDataCache.contentLink = link
        emitEffect(ContentSharingEffect.SelectChatsToUpload)
    }

    private suspend fun showNetworkAlertError() {
        emitEffect(ContentSharingEffect.ShowNetworkAlert)
    }

    private suspend fun completeNoError() {
        emitEffect(ContentSharingEffect.CloseSharingScreen)
    }

    private suspend fun showVideoDurationError() {
        emitEffect(ContentSharingEffect.ShowVideoDurationAlert)
    }

    private suspend fun completeOnFailure() {
        emitEffect(ContentSharingEffect.ShowWentWrongAlert)
        delay(ALERT_DURATION)
        emitEffect(ContentSharingEffect.CloseSharingScreen)
    }
}
