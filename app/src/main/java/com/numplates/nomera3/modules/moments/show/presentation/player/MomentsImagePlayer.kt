package com.numplates.nomera3.modules.moments.show.presentation.player

import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.meera.core.extensions.loadGlideWithCallback
import com.meera.core.extensions.release
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.presentation.DEFAULT_MOMENT_CONTENT_TIME_LENGTH_MS
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackListener
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlayerState
import com.numplates.nomera3.modules.moments.util.TimerWithPause

class MomentsImagePlayer(
    private val imageView: ImageView,
    private val loaderView: CircularProgressIndicator,
    private val momentPlaybackListener: MomentPlaybackListener
) : MomentsContentPlayer {

    private var progressCallback: ((currentProgress: Float) -> Unit)? = null
    private var updateTimer =
        ImageMomentProgressUpdateTimer(MOMENT_PROGRESS_ANIMATION_FRAME_TIME_MS)

    private var imagePlayerState = ImagePlayerState()

    override fun contentType() = MomentContentType.IMAGE

    override fun attachProgressCallback(callback: (currentProgress: Float) -> Unit) {
        progressCallback = callback
    }

    override fun loadResources(isActiveItem: Boolean, contentUrl: String?, previewUrl: String?) {
        if (contentUrl == null) return
        if (imagePlayerState.currentContentUrl == contentUrl) {
            if (imagePlayerState.isReady) momentPlaybackListener.onResourceReady()
            return
        }
        imagePlayerState.isReady = false
        imagePlayerState.currentContentUrl = contentUrl
        updateTimer.cancel()
        loadImage()
    }

    override fun togglePlayerState(state: MomentPlayerState) {
        when (state) {
            is MomentPlayerState.Start -> {
                imagePlayerState.playWhenReady = true
                seekToStart()
                checkPlayWhenReady()
            }
            is MomentPlayerState.Resume -> {
                imagePlayerState.playWhenReady = true
                checkPlayWhenReady()
            }
            is MomentPlayerState.Pause -> {
                imagePlayerState.playWhenReady = false
                checkPlayWhenReady()
            }
            is MomentPlayerState.Stop -> {
                imagePlayerState.playWhenReady = false
                stop()
            }
            is MomentPlayerState.Hide -> {
                imagePlayerState.reset()
                updateTimer.cancel()
                imageView.release()
            }
        }
    }

    override fun changeContentVisibility(isVisible: Boolean) {
        imageView.isVisible = isVisible
    }

    override fun detachProgressCallback() {
        progressCallback = null
        updateTimer.cancel()
    }

    override fun releasePlayer() {
        detachProgressCallback()
        imageView.release()
        imagePlayerState.reset()
    }

    private fun loadImage() {
        imageView.loadGlideWithCallback(
            onReady = {
                momentPlaybackListener.onResourceReady()
                imagePlayerState.isReady = true
                loaderView.hide()
                checkPlayWhenReady()
            },
            onError = {
                momentPlaybackListener.onResourceError()
                imagePlayerState.currentContentUrl = null
                imagePlayerState.isReady = false
            },
            path = imagePlayerState.currentContentUrl
        )
    }

    private fun checkPlayWhenReady() {
        when {
            imagePlayerState.isReady && imagePlayerState.playWhenReady -> play()
            imagePlayerState.isReady && imagePlayerState.playWhenReady.not() -> pause()
        }
    }

    private fun seekToStart() {
        updateTimer.cancel()
    }

    private fun play() {
        if (updateTimer.isPlaying.not()) {
            updateTimer.resume()
            momentPlaybackListener.onResumePlayback()
        }
    }

    private fun pause() {
        updateTimer.pause()
        momentPlaybackListener.onPausePlayback()
    }

    private fun stop() {
        updateTimer.cancel()
    }

    inner class ImageMomentProgressUpdateTimer(intervalMs: Long) : TimerWithPause(
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

    private class ImagePlayerState(
        var currentContentUrl: String? = null,
        var playWhenReady: Boolean = false,
        var isReady: Boolean = false
    ) {
        fun reset() {
            currentContentUrl = null
            playWhenReady = false
            isReady = false
        }
    }
}
