package com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller

import android.os.Handler
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.core.view.updateLayoutParams
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.getScreenWidth
import com.numplates.nomera3.modules.viewvideo.presentation.VideoScreenState
import com.numplates.nomera3.modules.viewvideo.presentation.exoplayer.ViewVideoExoPlayerManager
import com.numplates.nomera3.modules.viewvideo.presentation.exoplayer.ViewVideoExoPlayerWrapper
import com.numplates.nomera3.modules.viewvideo.presentation.view.ViewVideoSeekBarView
import com.numplates.nomera3.modules.viewvideo.presentation.view.getCurrentPosition

private const val UNKNOWN_ASPECT = -1.0
private const val RETRY_PLAY_DELAY = 2_000L

class ViewVideoPlaybackController(
    private val playerView: PlayerView,
    private val playerManager: ViewVideoExoPlayerManager,
    private val videoTimeBar: ViewVideoSeekBarView?,
    private val tryResumePlaybackHandler: Handler?,
    private val onSeekToMs: (Long) -> Unit,
    private val onResume: () -> Unit

) {

    private val aspectHelper = ViewVideoPlaybackControllerAspectHelper()

    private var wrapper: ViewVideoExoPlayerWrapper? = null
    private var token: String? = null

    private var appliedInfo: ViewVideoPlayerInfoModel = ViewVideoPlayerInfoModel()
    private var savedInfo: ViewVideoPlayerInfoModel = appliedInfo

    private var seekToMs: Long = 0
    private var isPlayerAttached = false
    private var isPlayerActive = false
    private var isNeedToPlay = true

    private var lastErrorPlaybackPosition = 0L
    private var tryResumePlaybackRunnable = Runnable { tryResumePlaybackActions() }

    private var screenAspect: Double = getScreenWidth() / getScreenHeight().toDouble()

    fun setVideoData(info: ViewVideoPlayerInfoModel) {
        isNeedToPlay = info.isVideoNeedToPlay ?: true
        setAspectSettings(info.aspect)
        if (isPlayerAttached) {
            applyVideoInfo(info)
            saveVideoInfo(info)
            if (isPlayerActive) startPlayback()
        } else {
            saveVideoInfo(info)
        }
    }

    fun seekToX(x: Int) {
        val player = wrapper ?: return
        val timeBar = videoTimeBar ?: return
        val totalDuration = player.duration()
        val actualPosition = timeBar.getCurrentPosition(positionX = x, totalDuration = totalDuration)
        onSeekToMs.invoke(actualPosition)
        seekToMs = actualPosition
    }

    fun seekTo(position: Long) {
        wrapper?.seekTo(position)
    }

    fun getLastPosition(): Long {
        return if (seekToMs != 0L) seekToMs else getCurrentPosition()
    }

    fun getCurrentPosition() = wrapper?.currentPosition() ?: 0
    fun getDuration() = wrapper?.duration() ?: 0

    fun resume(fromPosition: Long? = null) {
        fromPosition?.let { position ->
            seekToMs = position
            seekTo(position)
        }
        if (isPlayerActive || !isNeedToPlay) return
        isPlayerActive = true
        onResume.invoke()
        startPlayback()
    }

    fun pause() {
        if (isPlayerActive.not()) return
        isPlayerActive = false
        wrapper?.pause()
    }

    fun markAsPlayed() {
        lastErrorPlaybackPosition = 0L
        seekToMs = 0L
        stopResumePlaybackHandler()
    }

    fun tryResume() {
        stopResumePlaybackHandler()
        lastErrorPlaybackPosition = wrapper?.currentPosition() ?: 0L
        postTryResumePlaybackHandler()
    }

    fun stopResumePlaybackHandler() {
        tryResumePlaybackHandler?.removeCallbacksAndMessages(null)
    }

    fun onFragmentResumed(resumeFromMs: Long, isVideoNeedToPlay: Boolean? = null) {
        attachPlayer()
        if (seekToMs == 0L) seekToMs = resumeFromMs
        if (savedInfo != appliedInfo) setVideoData(savedInfo)
        isVideoNeedToPlay?.let { isNeedToPlay = it }
        if (isNeedToPlay) isPlayerActive = true
        startPlayback()
    }

    fun addPlayerListener(listener: Player.Listener) {
        wrapper?.addListener(listener)
    }

    fun removePlayerListener(listener: Player.Listener) {
        wrapper?.removeListener(listener)
    }

    fun onFragmentPaused(isPlayerAttached: Boolean = true) {
        stopResumePlaybackHandler()
        if (seekToMs == 0L) seekToMs = wrapper?.currentPosition() ?: 0
        wrapper?.pause()
        isPlayerActive = false
        this.isPlayerAttached = isPlayerAttached
        if (isPlayerAttached.not()) clearAppliedVideoInfo()
    }

    fun setNeedToPlay(isVideoNeedToPlay: Boolean) {
        isNeedToPlay = isVideoNeedToPlay
        savedInfo = savedInfo.copy(isVideoNeedToPlay = isVideoNeedToPlay)
    }

    fun resetVideoPlayback() {
        seekToMs = 0L
        wrapper?.seekTo(seekToMs)

        isNeedToPlay = true
        savedInfo = savedInfo.copy(isVideoNeedToPlay = isNeedToPlay)
    }

    fun onFragmentStopped() {
        stopResumePlaybackHandler()
        wrapper?.stop()
        wrapper?.clearPlayerWrapper()
        token = null
        clearAppliedVideoInfo()
    }

    private fun tryResumePlaybackActions() {
        val url = appliedInfo.videoUrl
        if (url != null) {
            wrapper?.prepare(url)
            if (seekToMs == 0L) {
                seekToMs = lastErrorPlaybackPosition
            }
        }
        startPlayback(isTryingPlay = true)

        postTryResumePlaybackHandler()
    }

    private fun postTryResumePlaybackHandler() {
        tryResumePlaybackHandler?.postDelayed(tryResumePlaybackRunnable, RETRY_PLAY_DELAY)
    }

    private fun startPlayback(isTryingPlay: Boolean = false) {
        if (seekToMs != 0L && appliedInfo.videoUrl != null) {
            wrapper?.seekTo(seekToMs)
            if (!isTryingPlay) seekToMs = 0L
        }
        if (!isNeedToPlay) return
        wrapper?.resume()
    }

    private fun attachPlayer() {
        wrapper = playerManager.attachPlayerView(
            playerView = playerView,
            token = token,
            interruptPlayback = true
        ).also {
            token = it.token
            isPlayerAttached = true
        }
    }

    private fun applyVideoInfo(info: ViewVideoPlayerInfoModel) {
        setVolumeState(info.isVolumeEnabled)
        prepareVideo(info.videoUrl)
    }

    private fun saveVideoInfo(info: ViewVideoPlayerInfoModel) {
        savedInfo = info
    }

    private fun clearAppliedVideoInfo() {
        appliedInfo = appliedInfo.copy(
            videoUrl = null,
            isVolumeEnabled = null
        )
    }

    private fun setAspectSettings(videoAspect: Double?) {
        when {
            videoAspect == null -> aspectHelper.setupUnknownAspect()
            videoAspect < screenAspect -> aspectHelper.setupTallAspect(videoAspect)
            else -> aspectHelper.setupNormalAspect(videoAspect)
        }
        appliedInfo = appliedInfo.copy(aspect = videoAspect ?: UNKNOWN_ASPECT)
    }

    private fun setVolumeState(isEnabled: Boolean?) {
        if (isEnabled == appliedInfo.isVolumeEnabled || isEnabled == null) return
        if (isEnabled) wrapper?.enableSound() else wrapper?.disableSound()
        appliedInfo = appliedInfo.copy(isVolumeEnabled = isEnabled)
    }

    private fun prepareVideo(contentUrl: String?) {
        if (contentUrl == null || appliedInfo.videoUrl == contentUrl) return
        wrapper?.prepare(contentUrl)
        appliedInfo = appliedInfo.copy(videoUrl = contentUrl)
    }

    private inner class ViewVideoPlaybackControllerAspectHelper {

        fun setupTallAspect(aspect: Double) {
            val displayWidth = (playerView.parent as ViewGroup).width
            val neededWidth = (playerView.parent as ViewGroup).height * aspect
            playerView.updateLayoutParams<LayoutParams> {
                height = LayoutParams.MATCH_PARENT
                width = if (displayWidth <= neededWidth) {
                    LayoutParams.MATCH_PARENT
                } else {
                    neededWidth.toInt()
                }
            }
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        }

        fun setupNormalAspect(aspect: Double) {
            playerView.updateLayoutParams<LayoutParams> {
                width = LayoutParams.MATCH_PARENT
                height = ((playerView.parent as ViewGroup).width / aspect).toInt()
            }
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }

        fun setupUnknownAspect() {
            playerView.updateLayoutParams<LayoutParams> {
                width = LayoutParams.MATCH_PARENT
                height = LayoutParams.MATCH_PARENT
            }
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

}

data class ViewVideoPlayerInfoModel(
    val videoUrl: String? = null,
    val isVolumeEnabled: Boolean? = true,
    val isVideoNeedToPlay: Boolean? = true,
    val aspect: Double? = null
)

fun VideoScreenState.VideoInfo.toPlayerInfo() = ViewVideoPlayerInfoModel(
    videoUrl = videoUrl,
    isVolumeEnabled = isVolumeEnabled,
    isVideoNeedToPlay = isVideoNeedToPlay,
    aspect = videoAspect
)
