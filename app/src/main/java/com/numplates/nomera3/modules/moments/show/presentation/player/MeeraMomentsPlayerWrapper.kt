package com.numplates.nomera3.modules.moments.show.presentation.player

import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.view.progress.ExactProgressMomentsView
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MeeraPositionViewMomentState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlayerState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentTimelineState


interface MeeraMomentsContentPlayer {
    fun contentType(): MomentContentType
    fun attachProgressCallback(callback: (currentProgress: Float) -> Unit)
    fun loadResources(isActiveItem: Boolean, contentUrl: String?, previewUrl: String?)
    fun togglePlayerState(state: MomentPlayerState)
    fun changeContentVisibility(isVisible: Boolean)
    fun detachProgressCallback()
    fun releasePlayer()
}

class MeeraMomentsPlayerWrapper(
    private val momentPlayers: List<MeeraMomentsContentPlayer>,
    private val progressView: ExactProgressMomentsView?
) {

    private val handleProgressUpdate: ((progress: Float) -> Unit) = ::handleProgressUpdate

    private var currentPlayer: MeeraMomentsContentPlayer? = null
    private var currentContentType: MomentContentType? = null
    private var currentTimelineState: MomentTimelineState? = null

    fun setupMomentContent(state: MeeraPositionViewMomentState.UpdateMoment) {
        val newMomentData = state.momentItemModel ?: return
        val newPlaybackState = state.playbackState ?: return
        val newTimelineState = state.timelineState ?: return
        val newContentType = newMomentData.getContentTypeForPlayer()
        handleContentTypeUpdate(newContentType)
        handleTimelineStateUpdate(newTimelineState)
        handleResourceLoading(
            loadResources = state.loadResources,
            isActiveItem = state.isActiveItem,
            contentUrl = newMomentData.contentUrl,
            previewUrl = newMomentData.contentPreview
        )
        handlePlaybackStateUpdate(newPlaybackState)
    }

    fun setupErrorContent(errorState: MeeraPositionViewMomentState.UpdateMoment) {
        val newTimelineState = errorState.timelineState ?: return
        val newPlaybackState = errorState.playbackState ?: return
        val error = errorState.error ?: return

        handleContentTypeUpdate(MomentContentType.UNAVAILABLE)
        handleTimelineStateUpdate(newTimelineState)
        handlePlaybackStateUpdate(newPlaybackState)

        val unavailablePlayer = currentPlayer as? MomentsUnavailablePlayer
        unavailablePlayer?.setErrorState(error)
    }

    fun updatePlaybackState(playbackState: MomentPlaybackState) {
        handlePlaybackStateUpdate(playbackState)
    }

    fun releasePlayers() {
        momentPlayers.forEach { it.releasePlayer() }
    }

    private fun handleContentTypeUpdate(newContentType: MomentContentType) {
        if (currentContentType != newContentType) {
            toggleContentPlayer(newContentType)
        }
    }

    private fun handleTimelineStateUpdate(timelineState: MomentTimelineState) {
        if (currentTimelineState != timelineState) {
            currentTimelineState = timelineState
            progressView?.progressBarCount = timelineState.totalBars
        }
    }

    private fun handleResourceLoading(loadResources: Boolean, isActiveItem: Boolean, contentUrl: String?, previewUrl: String?) {
        if (loadResources) {
            handleProgressUpdate(0f)
            currentPlayer?.loadResources(isActiveItem = isActiveItem, contentUrl = contentUrl, previewUrl = previewUrl)
        }
    }

    private fun handlePlaybackStateUpdate(newPlaybackState: MomentPlaybackState) {
        when (newPlaybackState) {
            MomentPlaybackState.PAUSED -> {
                togglePlayerState(MomentPlayerState.Pause)
            }
            MomentPlaybackState.RESUMED -> {
                togglePlayerState(MomentPlayerState.Resume)
            }
            MomentPlaybackState.STARTED -> {
                togglePlayerState(MomentPlayerState.Start)
            }
            MomentPlaybackState.STOPPED -> {
                togglePlayerState(MomentPlayerState.Stop)
                handleProgressUpdate(0f)
            }
        }
    }

    private fun togglePlayerState(newState: MomentPlayerState) {
        currentPlayer?.togglePlayerState(newState)
    }

    private fun handleProgressUpdate(progress: Float) {
        val currentBar = currentTimelineState?.currentBar ?: return
        progressView?.setCurrentProgress(currentBar, progress)
    }

    private fun toggleContentPlayer(newContentType: MomentContentType) {
        momentPlayers.forEach { player ->
            val isActivePlayer = player.contentType() == newContentType
            player.changeContentVisibility(isVisible = isActivePlayer)
            if (isActivePlayer) {
                currentPlayer = player
                player.attachProgressCallback(handleProgressUpdate)
            } else {
                player.detachProgressCallback()
                player.togglePlayerState(MomentPlayerState.Hide)
            }
        }
    }
}

private fun MomentItemUiModel.getContentTypeForPlayer(): MomentContentType {
    return when {
        isUserBlackListByMe || isUserBlackListMe || !isActive || isDeleted
            || isAccessDenied || contentUrl == null -> MomentContentType.UNAVAILABLE
        contentType == MomentContentType.VIDEO.value -> MomentContentType.VIDEO
        contentType == MomentContentType.IMAGE.value -> MomentContentType.IMAGE
        else -> error("Unsupported content type")
    }
}
