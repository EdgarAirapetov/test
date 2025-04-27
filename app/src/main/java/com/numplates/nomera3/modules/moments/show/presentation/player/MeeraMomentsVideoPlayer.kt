package com.numplates.nomera3.modules.moments.show.presentation.player

import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.loadGlideWithCallback
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackListener
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlayerState

class MeeraMomentsVideoPlayer(
    private val videoView: PlayerView,
    private val previewView: ImageView,
    private val loaderView: CircularProgressIndicator,
    private val momentPlaybackListener: MomentPlaybackListener,
    private val playerHandler: MomentsExoPlayerManager
) : MeeraMomentsContentPlayer {

    private val videoProgressPollingListener = VideoMomentProgressPollingListener()
    private val videoPlaybackStateListener = VideoMomentPlaybackStateListener()
    private var progressCallback: ((currentProgress: Float) -> Unit)? = null

    private var playerToken: String? = null
    private var currentContentUrl: String? = null
    private var isActiveItem = false
    private var shouldShowPreview = true
    private var exoPlayerWrapper: MomentsExoPlayerWrapper? = null

    override fun contentType() = MomentContentType.VIDEO

    override fun attachProgressCallback(callback: (currentProgress: Float) -> Unit) {
        progressCallback = callback
    }

    override fun loadResources(isActiveItem: Boolean, contentUrl: String?, previewUrl: String?) {
        if (contentUrl == null) return
        setIsActiveItem(isActiveItem)
        when {
            isActiveItem && isNewContent(contentUrl) -> prepareVideo(contentUrl)
            else -> prepareImagePreview(previewUrl)
        }
    }

    override fun togglePlayerState(state: MomentPlayerState) {
        if (state != MomentPlayerState.Hide && checkIfPlayerInitialized().not()) return
        when (state) {
            is MomentPlayerState.Start -> {
                exoPlayerWrapper?.start()
            }
            is MomentPlayerState.Resume -> {
                exoPlayerWrapper?.resume()
            }
            is MomentPlayerState.Pause -> {
                exoPlayerWrapper?.pause()
            }
            is MomentPlayerState.Stop -> {
                exoPlayerWrapper?.stop()
            }
            is MomentPlayerState.Hide -> {
                exoPlayerWrapper?.stop()
                currentContentUrl = null
            }
        }
    }

    override fun changeContentVisibility(isVisible: Boolean) {
        videoView.isVisible = isVisible
        if (isVisible) {
            previewView.isVisible = shouldShowPreview
        } else {
            previewView.isVisible = false
        }
    }

    override fun detachProgressCallback() {
        progressCallback = null
    }

    override fun releasePlayer() {
        clearPlayerStateListeners()
        exoPlayerWrapper?.clearPlayerWrapper()
        currentContentUrl = null
    }

    private fun prepareImagePreview(previewUrl: String?) {
        if (previewUrl == null) return
        showPreview()
        previewView.loadGlideWithCallback(
            onReady = {
                momentPlaybackListener.onResourceReady(isPreview = true)
            },
            path = previewUrl
        )
    }

    private fun prepareVideo(mediaUrl: String) {
        exoPlayerWrapper?.let { wrapper ->
            wrapper.prepare(mediaUrl)
            currentContentUrl = mediaUrl
            initPlayerStateListener()
        }
    }

    private fun checkIfPlayerInitialized(): Boolean {
        return if (currentContentUrl == null && exoPlayerWrapper?.playbackState() == Player.STATE_IDLE) {
            momentPlaybackListener.onResourceError()
            false
        } else {
            true
        }
    }

    private fun setIsActiveItem(isActive: Boolean) {
        if (isActiveItem == isActive) return
        if (isActiveItem) {
            onDetachVideoPlayer()
        } else {
            onAttachVideoPlayer()
        }
        isActiveItem = isActive
    }

    private fun onAttachVideoPlayer() {
        exoPlayerWrapper = playerHandler.attachPlayerView(playerView = videoView, token = playerToken, interruptPlayback = true)
        playerToken = exoPlayerWrapper?.token
        initPlayerStateListener()
    }

    private fun onDetachVideoPlayer() {
        currentContentUrl = null
        clearPlayerStateListeners()
        exoPlayerWrapper?.clearPlayerWrapper()
    }

    private fun clearPlayerStateListeners() {
        videoProgressPollingListener.removeCallbacks()
        exoPlayerWrapper?.removeListener(videoProgressPollingListener)
        exoPlayerWrapper?.removeListener(videoPlaybackStateListener)
    }

    private fun initPlayerStateListener() {
        exoPlayerWrapper?.addListener(videoProgressPollingListener)
        exoPlayerWrapper?.addListener(videoPlaybackStateListener)
    }

    private fun hidePreview() {
        previewView.isVisible = false
        previewView.glideClear()
        shouldShowPreview = false
    }

    private fun showPreview() {
        previewView.isVisible = true
        shouldShowPreview = true
    }

    private fun isNewContent(contentUrl: String): Boolean = contentUrl != currentContentUrl

    inner class VideoMomentPlaybackStateListener : Player.Listener {

        override fun onRenderedFirstFrame() {
            hidePreview()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    momentPlaybackListener.onResourceReady()
                    loaderView.hide()
                }
                Player.STATE_ENDED -> {
                    currentContentUrl = null
                    momentPlaybackListener.onPlaybackEnded()
                }
                else -> Unit
            }
        }
    }

    inner class VideoMomentProgressPollingListener : Player.Listener {

        private val updateProgress: () -> Unit = ::updateProgress

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                removeCallbacks()
                videoView.postDelayed(updateProgress, MOMENT_PROGRESS_ANIMATION_FRAME_TIME_MS)
                momentPlaybackListener.onResumePlayback()
            } else {
                momentPlaybackListener.onPausePlayback()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                removeCallbacks()
                progressCallback?.invoke(1f)
            }
        }

        fun removeCallbacks() {
            videoView.removeCallbacks(updateProgress)
        }

        private fun updateProgress() {
            val view = videoView
            val progress = exoPlayerWrapper?.currentProgress() ?: 0f
            progressCallback?.invoke(progress)
            if (exoPlayerWrapper?.isPlaying() == true) {
                view.postDelayed(updateProgress, MOMENT_PROGRESS_ANIMATION_FRAME_TIME_MS)
            }
        }
    }
}
