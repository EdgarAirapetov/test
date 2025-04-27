package com.numplates.nomera3.modules.newroads

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.exoplayer2.ExoPlayer
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.widgets.VideoRetryView

class VideoFeedHelper(
    private val context: Context,
    private val volumeStateCallback: VolumeStateCallback
) {
    private var videoPlayer: ExoPlayer? = null
    private var retryView: VideoRetryView? = null
    private var currentVideo = ""
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0

    private var videoHolder: VideoViewHolder? = null

    private val volumeListeners: MutableSet<()->Unit> = mutableSetOf()

    fun addVolumeSwitchListener(listener: () -> Unit) {
        volumeListeners.add(listener)
    }

    fun removeVolumeSwitchListener(listener: () -> Unit) {
        volumeListeners.remove(listener)
    }

    fun init() {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

        retryView = VideoRetryView(context.applicationContext)

        retryView?.setOnClickListener {
            if (videoHolder?.needToPlay() == false) {
                removeCommonView(retryView)
                return@setOnClickListener
            }
            clearCacheForCurrentVideo()
            startVideo()
        }
    }

    fun isVideoPlaying() = videoPlayer?.isPlaying.isTrue()

    fun getCurrentPosition(): Long = videoPlayer?.currentPosition ?: 0
    fun getDuration(): Long = videoPlayer?.duration ?: 0

    private fun clearCacheForCurrentVideo() {
        Act.simpleCache?.removeResource(currentVideo)
    }

    private fun startVideo(mediaPosition: Long? = null) {
        videoPlayer = videoHolder?.getVideoPlayerView()?.player as? ExoPlayer?
        videoHolder?.startPlayingVideo(mediaPosition)
    }

    private fun removeCommonView(view: View?) {
        val parent = view?.parent as? ViewGroup ?: return
        val index = parent.indexOfChild(view)
        if (index >= 0) {
            parent.removeViewAt(index)
        }
    }

    fun setVideoHolder(holder: VideoViewHolder) {
        videoHolder = holder
    }

    fun playVideo(mediaPosition: Long? = null) {
        if (videoHolder == null || videoHolder?.holderIsNotAttachedToWindow().isTrue()) {
            return
        }
        videoHolder?.onShowPostClicked = {
            playVideo()
        }
        if (videoHolder?.needToPlay() == false) {
            return
        }
        val mediaUrl = videoHolder?.getVideoUrlString()

        if (mediaUrl.isNullOrEmpty()) return

        val isPlaying = (videoHolder?.getVideoPlayerView()?.player as? ExoPlayer?)?.playWhenReady.isTrue()

        if (mediaUrl == currentVideo && isPlaying) return
        this.currentVideo = mediaUrl

        startVideo(mediaPosition)
    }

    fun turnOffAudioOfVideo() {
        volumeStateCallback.setVolumeState(VolumeState.OFF)
    }

    fun pauseVideo() {
        videoHolder?.stopPlayingVideo()
    }

    fun onStop() {
        videoHolder?.stopPlayingVideo()
    }

    fun onStart(lastPostMediaViewInfo: PostMediaViewInfo?) {
        playVideo(lastPostMediaViewInfo?.lastVideoPlaybackPosition)
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }
        if (videoHolder != null) {
            videoHolder?.detachPlayer()
            videoHolder = null
        }
        retryView = null
    }
}
