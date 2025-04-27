package com.numplates.nomera3.modules.moments.show.presentation.player

import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutMomentUnavailableBinding
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.presentation.DEFAULT_MOMENT_CONTENT_TIME_LENGTH_MS
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentPositionViewModel
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackListener
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlayerState
import com.numplates.nomera3.modules.moments.util.TimerWithPause

class MomentsUnavailablePlayer(
    private val layoutBinding: LayoutMomentUnavailableBinding,
    private val loaderView: CircularProgressIndicator,
    private val momentPlaybackListener: MomentPlaybackListener
) : MomentsContentPlayer {

    private var progressCallback: ((currentProgress: Float) -> Unit)? = null
    private var updateTimer = UnavailableMomentProgressUpdateTimer(MOMENT_PROGRESS_ANIMATION_FRAME_TIME_MS)

    override fun contentType(): MomentContentType = MomentContentType.UNAVAILABLE

    override fun attachProgressCallback(callback: (currentProgress: Float) -> Unit) {
        progressCallback = callback
    }

    override fun loadResources(isActiveItem: Boolean, contentUrl: String?, previewUrl: String?) {
        loaderView.hide()
        momentPlaybackListener.onResourceReady()
        updateTimer.cancel()
    }

    override fun togglePlayerState(state: MomentPlayerState) {
        when (state) {
            is MomentPlayerState.Start -> start()
            is MomentPlayerState.Resume -> resume()
            is MomentPlayerState.Pause -> pause()
            is MomentPlayerState.Stop,
            is MomentPlayerState.Hide -> stop()
        }
    }

    override fun changeContentVisibility(isVisible: Boolean) {
        layoutBinding.root.isVisible = isVisible
    }

    override fun detachProgressCallback() {
        progressCallback = null
        updateTimer.cancel()
    }

    override fun releasePlayer() {
        detachProgressCallback()
    }

    fun setErrorState(errorState: ViewMomentPositionViewModel.ErrorState) {
        when (errorState) {
            ViewMomentPositionViewModel.ErrorState.MomentNotFound -> {
                layoutBinding.tvMomentUnavailableMessage.setText(R.string.moments_unavailable_time_out_message)
            }
            ViewMomentPositionViewModel.ErrorState.UnknownError -> {
                layoutBinding.tvMomentUnavailableMessage.setText(R.string.moments_unavailable_unknown_error_message)
            }
        }
    }

    private fun resume() {
        if (updateTimer.isPlaying.not()) {
            updateTimer.resume()
            momentPlaybackListener.onResumePlayback()
        }
    }

    private fun start() {
        updateTimer.cancel()
        updateTimer.start()
    }

    private fun pause() {
        updateTimer.pause()
        momentPlaybackListener.onPausePlayback()
    }

    private fun stop() {
        updateTimer.cancel()
    }

    inner class UnavailableMomentProgressUpdateTimer(intervalMs: Long) : TimerWithPause(
        totalTimerLengthMs = DEFAULT_MOMENT_CONTENT_TIME_LENGTH_MS,
        intervalMs = intervalMs
    ) {

        override fun onTotalProgress(progress: Float) {
            progressCallback?.invoke(progress)
        }

        override fun onFinishCompletely() {
            progressCallback?.invoke(1f)
            momentPlaybackListener.onPlaybackEnded()
        }
    }
}
