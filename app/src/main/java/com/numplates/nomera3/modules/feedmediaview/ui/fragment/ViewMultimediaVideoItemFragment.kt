package com.numplates.nomera3.modules.feedmediaview.ui.fragment

import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.isVisible
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.keepScreenOnDisable
import com.meera.core.extensions.keepScreenOnEnable
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewMultimediaVideoItemBinding
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerPlaybackStateListener
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.viewvideo.presentation.ARG_VIEW_VIDEO_DATA
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoGestureDetector
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.modules.viewvideo.presentation.exoplayer.ViewVideoExoPlayerManager
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoHideUiSwipeController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlaybackController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlayerInfoModel
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoSeekController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoZoomController

private const val MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY = 3_000L
private const val LOADER_SHOW_DELAY = 1_000L
private const val UPDATE_PLAYBACK_POSITION_PERIOD = 50L

private const val PROGRESS_START_FRAME = 42
private const val PROGRESS_END_FRAME = 78

class ViewMultimediaVideoItemFragment : MeeraBaseFragment(
    layout = R.layout.fragment_view_multimedia_video_item
), ViewMultimediaGesturesListener {

    private val binding by viewBinding(FragmentViewMultimediaVideoItemBinding::bind)

    private var currentItem: MediaAssetEntity? = null

    private var gestureDetector: ViewVideoGestureDetector? = null
    private var viewVideoPlaybackController: ViewVideoPlaybackController? = null
    private var seekVideoController: ViewVideoSeekController? = null
    private var zoomVideoController: ViewVideoZoomController? = null
    private var hideSwipeVideoController: ViewVideoHideUiSwipeController? = null
    private var exoPlayerManager: ViewVideoExoPlayerManager? = null

    private var loaderHandler: Handler? = null
    private var doubleTapHandler: Handler? = null
    private var tryResumePlaybackHandler: Handler? = null
    private var playbackPositionHandler: Handler? = null
    private var updatePlaybackPositionRunnable = Runnable { updatePlaybackPositionActions() }

    private var isVideoNeedToPlay: Boolean = true
    private var isVolumeEnabled: Boolean = true
    private var isCommonLongTapActive = false

    private var videoInitialData: ViewVideoInitialData? = null
    private var videoDuration: Long? = null

    private var actionListener: ViewMultimediaActionListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initContentData()
        if (itemIsUnavailable()) return
        initHandlers()
        initGestureDetector()
        initSeekVideoController()
        initZoomVideoController()
        enablePlayingListener()
        initProgressBar()

        updateTimelineVisibility((currentItem?.duration?.toLong() ?: 0) * 1000)
        initPlayer()

        setupVideoData()
    }

    private fun initProgressBar() {
        binding.cpiViewMultimediaVideoLoader.setMinAndMaxFrame(PROGRESS_START_FRAME, PROGRESS_END_FRAME)
    }

    private fun setProgressState(needShow: Boolean) {
        binding.cpiViewMultimediaVideoLoader.apply {
            isVisible = needShow
            if (needShow) playAnimation() else cancelAnimation()
        }
    }

    private fun itemIsUnavailable(): Boolean {
        if (currentItem?.isAvailable == false) {
            showUnavailableView()
            return true
        } else {
            return false
        }
    }

    private fun showUnavailableView() {
        binding.apply {
            incViewMultimediaUnavailableLayout.tvMediaUnavailableHeader.text = getString(R.string.video_unavailable_default_title)
            incViewMultimediaUnavailableLayout.root.visible()
            vvssvViewMultimediaVideoTimeBarSmall.gone()
            vvsbvViewMultimediaVideoTimeBar.gone()
        }

    }

    override fun onResume() {
        super.onResume()
        onFragmentResumed()
    }

    override fun onPause() {
        super.onPause()
        onFragmentPaused()
    }

    override fun onStop() {
        super.onStop()
        onFragmentPaused()
        disableDisplayAlwaysOn()
        viewVideoPlaybackController?.onFragmentStopped()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disableDisplayAlwaysOn()
        stopLoaderHandler()
        stopPositionUpdate()
        stopDoubleTapHandler()
        exoPlayerManager?.releasePlayer()
    }

    override fun disableGestures() {
        gestureDetector?.setTouchEventsEnabled(false)
        hideTimeBar()
        disablePlayingListener()
    }

    override fun enableGestures() {
        gestureDetector?.setTouchEventsEnabled(true)
        enablePlayingListener()
    }

    private fun hideTimeBar() {
        binding.vvssvViewMultimediaVideoTimeBarSmall.gone()
    }

    fun bind(actionListener: ViewMultimediaActionListener) {
        this.actionListener = actionListener
    }

    fun initTimelineViews(videoInitialData: ViewVideoInitialData?) {
        this.videoInitialData = videoInitialData
        videoInitialData?.let { data ->
            binding.apply {
                updateTimelineVisibility(data.duration)

                vvssvViewMultimediaVideoTimeBarSmall.setPosition(data.position)
                vvsbvViewMultimediaVideoTimeBar.setPosition(data.position)

                setupTimelineDurations(data.duration)

                viewVideoPlaybackController?.seekTo(data.position)
            }
        }
    }

    fun resumeVideo() {
        if (!isVideoNeedToPlay) return
        viewVideoPlaybackController?.resume()
    }

    fun resetVideoPlayback() {
        isVideoNeedToPlay = true
        onPlayingStateChanged(isVideoNeedToPlay)
        videoInitialData = videoInitialData?.copy(position = 0)
        viewVideoPlaybackController?.resetVideoPlayback()
    }

    fun resumePlayback() {
        viewVideoPlaybackController?.resume()
    }

    fun pausePlayback() {
        viewVideoPlaybackController?.pause()
    }

    fun getCurrentVideoPlaybackPosition() = viewVideoPlaybackController?.getLastPosition()

    private fun initGestureDetector() {
        val gestureDetector = ViewVideoGestureDetector(
            context = requireContext(),
            doubleTapHandler = doubleTapHandler,
            timeBar = binding.vvssvViewMultimediaVideoTimeBarSmall,
            listener = object : ViewVideoGestureDetector.Listener {
                override fun onTap() = Unit

                override fun onTapInsideTimeBar() {
                    actionListener?.disableTouchEvent()
                }

                override fun onTapReleased() {
                    checkVideoAvailableAction(action = { resumePlayback() })
                    actionListener?.enableTouchEvent()
                    actionListener?.showActionViews()
                }

                override fun onLongTap() {
                    if (gestureDetector?.isTouchesEnabled().isFalse()) return
                    checkVideoAvailableAction(action = {
                        isCommonLongTapActive = true
                        pausePlayback()
                        binding.ivViewMultimediaVideoPlayIndicator.gone()
                        actionListener?.apply {
                            hideActionViews()
                            disableTouchEvent()
                        }
                        binding.apply {
                            vvssvViewMultimediaVideoTimeBarSmall.gone()
                            vViewMultimediaVideoBottomGradient.gone()
                        }
                    })
                }

                override fun onLongTapTimeBar(tapPoint: Point) {
                    checkVideoAvailableAction(action = {
                        pausePlayback()
                        seekVideoController?.show(tapPoint)
                        seekControllerToPosition(positionX = tapPoint.x)
                        actionListener?.apply {
                            hideActionViews()
                            disableTouchEvent()
                        }
                        binding.vvssvViewMultimediaVideoTimeBarSmall.gone()
                    })
                }

                override fun onDoubleTap() = Unit

                override fun seekTo(positionX: Int) {
                    checkVideoAvailableAction(action = {
                        seekControllerToPosition(positionX)
                    })
                }

                override fun onLongTapReleased() {
                    checkVideoAvailableAction(action = {
                        isCommonLongTapActive = false
                        seekVideoController?.hide()
                        actionListener?.apply {
                            showActionViews()
                            enableTouchEvent()
                        }
                        binding.apply {
                            vViewMultimediaVideoBottomGradient.visible()
                            vvssvViewMultimediaVideoTimeBarSmall.visible()
                        }
                        showPlayIfNeeded()
                        val currentVideoPosition = viewVideoPlaybackController?.getLastPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onScaleBegin(focusX: Float, focusY: Float) {
                    checkVideoAvailableAction(action = {
                        resumePlayback()
                        zoomVideoController?.onScaleStart(focusX, focusY)
                        binding.apply {
                            ivViewMultimediaVideoPlayIndicator.gone()
                            vvssvViewMultimediaVideoTimeBarSmall.gone()
                            vViewMultimediaVideoBottomGradient.gone()
                        }
                        actionListener?.apply {
                            hideActionViews()
                            disableTouchEvent()
                        }
                    })
                }

                override fun onScale(scale: Float) {
                    checkVideoAvailableAction(action = { zoomVideoController?.onScale(scale) })
                }

                override fun onScaleEnd() {
                    checkVideoAvailableAction(action = {
                        resumePlayback()
                        zoomVideoController?.onScaleEnd()
                        actionListener?.apply {
                            showActionViews()
                            enableTouchEvent()
                        }
                        binding.apply {
                            vViewMultimediaVideoBottomGradient.visible()
                            vvssvViewMultimediaVideoTimeBarSmall.visible()
                        }
                        showPlayIfNeeded()
                    })
                }

                override fun onHorizontalSwipe(distanceX: Float) = Unit

                override fun onHorizontalSwipeEnded() {
                    checkVideoAvailableAction(action = {
                        seekVideoController?.hide()
                        actionListener?.apply {
                            showActionViews()
                            enableTouchEvent()
                        }
                        binding.apply {
                            vViewMultimediaVideoBottomGradient.visible()
                            vvssvViewMultimediaVideoTimeBarSmall.visible()
                        }
                        val currentVideoPosition = binding.vvsbvViewMultimediaVideoTimeBar.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalFling(velocityX: Float) {
                    checkVideoAvailableAction(action = {
                        seekVideoController?.hide()
                        actionListener?.apply {
                            showActionViews()
                            enableTouchEvent()
                        }
                        binding.apply {
                            vViewMultimediaVideoBottomGradient.visible()
                            vvssvViewMultimediaVideoTimeBarSmall.visible()
                        }
                        val currentVideoPosition = binding.vvsbvViewMultimediaVideoTimeBar.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalSwipeTimeBar(tapPoint: Point) {
                    checkVideoAvailableAction(action = {
                        pausePlayback()
                        seekVideoController?.show(tapPoint)
                        actionListener?.apply {
                            hideActionViews()
                            disableTouchEvent()
                        }
                        binding.vvssvViewMultimediaVideoTimeBarSmall.gone()
                    })
                }

                override fun onVerticalFling(velocityY: Float) {
                    resumePlayback()
                    hideSwipeVideoController?.onVerticalFling()
                }

                private fun pausePlayback() = viewVideoPlaybackController?.pause()

                private fun resumePlayback(position: Long? = null) {
                    if (isVideoNeedToPlay) viewVideoPlaybackController?.resume(position)
                }
            })
        this.gestureDetector = gestureDetector
        this.gestureDetector?.setTouchEventsEnabled(false)
        binding.vgViewMultimediaVideoGestures.setOnTouchListener { v, event -> gestureDetector.onTouchEvent(v, event) }
    }

    private fun seekControllerToPosition(positionX: Int) {
        if (isCommonLongTapActive) return
        viewVideoPlaybackController?.seekToX(positionX)
    }

    private fun initSeekVideoController() {
        binding.apply {
            seekVideoController = ViewVideoSeekController(
                timeDisplayView = tvViewMultimediaVideoTime,
                timeBar = vvsbvViewMultimediaVideoTimeBar,
                smallTimeBar = vvssvViewMultimediaVideoTimeBarSmall
            )
        }
    }

    private fun initZoomVideoController() {
        binding.apply {
            zoomVideoController = ViewVideoZoomController(pvViewMultimediaVideoPlayer)
        }
    }

    private fun enablePlayingListener() {
        binding.vgViewMultimediaVideoGestures.setOnClickListener {
            val isNeedToPlay = isVideoNeedToPlay.not()
            onPlayingStateChanged(isNeedToPlay)
            setVideoStatePlaying(isNeedToPlay)
        }
    }

    private fun disablePlayingListener() {
        binding.vgViewMultimediaVideoGestures.setOnClickListener(null)
    }

    private fun setVideoStatePlaying(isNeedToPlay: Boolean) {
        if (isVideoNeedToPlay == isNeedToPlay) return
        isVideoNeedToPlay = isNeedToPlay
        viewVideoPlaybackController?.setNeedToPlay(isNeedToPlay)

        if (isNeedToPlay) {
            viewVideoPlaybackController?.resume()
        } else {
            stopPositionUpdate()
            viewVideoPlaybackController?.pause()
        }
    }

    private fun onPlayingStateChanged(isNeedToPlay: Boolean) {
        binding.ivViewMultimediaVideoPlayIndicator.isVisible = !isNeedToPlay
    }

    private fun showPlayIfNeeded() {
        if (!isVideoNeedToPlay) binding.ivViewMultimediaVideoPlayIndicator.visible()
    }

    private fun setupTimelineDurations(duration: Long?) {
        if (duration == null) return
        binding.apply {
            vvssvViewMultimediaVideoTimeBarSmall.setDuration(duration)
            vvsbvViewMultimediaVideoTimeBar.setDuration(duration)
        }
    }

    private fun setupVideoData() {
        currentItem?.let {
            val playerInfo = ViewVideoPlayerInfoModel(
                videoUrl = it.video,
                isVolumeEnabled = isVolumeEnabled,
                isVideoNeedToPlay = isVideoNeedToPlay,
                aspect = it.aspect.toDouble()
            )

            viewVideoPlaybackController?.setVideoData(playerInfo)
        }
    }

    private fun onFragmentResumed() {
        viewVideoPlaybackController?.onFragmentResumed(
            resumeFromMs = videoInitialData?.position ?: 0,
            isVideoNeedToPlay = isVideoNeedToPlay
        )
        removeSeekListener()
        addSeekListener()
        clearInitialPlayerPosition()
    }

    private fun onFragmentPaused() {
        disableDisplayAlwaysOn()
        stopDoubleTapHandler()
        stopLoaderHandler()
        viewVideoPlaybackController?.onFragmentPaused(/*isPlayerAttached = if we were not scrolled offscreen in viewpager*/)
        removeSeekListener()
    }

    private fun addSeekListener() {
        val seekListener = seekVideoController?.providePlayerListenerInstance()
        if (seekListener != null) viewVideoPlaybackController?.addPlayerListener(seekListener)
    }

    private fun removeSeekListener() {
        val seekListener = seekVideoController?.providePlayerListenerInstance()
        if (seekListener != null) viewVideoPlaybackController?.removePlayerListener(seekListener)
    }

    private fun clearInitialPlayerPosition() {
        arguments?.putLong(ARG_VIEW_VIDEO_DATA, 0)
        videoInitialData = videoInitialData?.copy(position = 0)
    }

    private fun initPlayer() {
        binding.apply {
            val playbackListener = object : ExoPlayerPlaybackStateListener {
                override fun onLoading() { showLoaderForPlaybackState() }
                override fun onPause() { onPausePlaybackActions() }
                override fun onPlaying() { onPlayingActions() }
                override fun onError() { onErrorPlaybackActions() }
            }

            val manager = exoPlayerManager ?: ViewVideoExoPlayerManager(root.context, playbackListener)
                .also { exoPlayerManager = it }

            viewVideoPlaybackController = ViewVideoPlaybackController(
                playerView = pvViewMultimediaVideoPlayer,
                playerManager = manager,
                videoTimeBar = vvsbvViewMultimediaVideoTimeBar,
                tryResumePlaybackHandler = tryResumePlaybackHandler,
                onSeekToMs = { currentPosition -> seekVideoController?.updateProgress(currentPosition) },
                onResume = { enableDisplayAlwaysOn() }
            )
        }
    }

    private fun initHandlers() {
        loaderHandler = Handler(Looper.getMainLooper())
        playbackPositionHandler = Handler(Looper.getMainLooper())
        tryResumePlaybackHandler = Handler(Looper.getMainLooper())
        doubleTapHandler = Handler(Looper.getMainLooper())
    }

    private fun onPlayingActions() {
        checkVideoAvailableAction(
            action = { onPlayingConfirmActions() },
            stopAction = { handleStopPlayer() }
        )
    }

    private fun onPlayingConfirmActions() {
        viewVideoPlaybackController?.tryResume()
        viewVideoPlaybackController?.markAsPlayed()
        hideLoaderForPlaybackState()
        updatePlaybackPosition()
        val videoDuration = viewVideoPlaybackController?.getDuration()
        doDelayed(UPDATE_PLAYBACK_POSITION_PERIOD) { updateTimelineVisibility(duration = videoDuration) }
        enableDisplayAlwaysOn()
    }

    private fun onPausePlaybackActions() {
        disableDisplayAlwaysOn()
        stopPositionUpdate()
    }

    private fun onErrorPlaybackActions() {
        disableDisplayAlwaysOn()
        viewVideoPlaybackController?.tryResume()
    }

    private fun checkVideoAvailableAction(action: () -> Unit, stopAction: () -> Unit = {}) {
        val isVideoAvailable = !currentItem?.video.isNullOrEmpty()
        if (isVideoAvailable) {
            action.invoke()
        } else {
            stopAction.invoke()
        }
    }

    private fun hideLoaderForPlaybackState() {
        stopLoaderHandler()
        setProgressState(needShow = false)
    }

    private fun updatePlaybackPosition() {
        playbackPositionHandler?.apply {
            removeCallbacksAndMessages(null)
            postDelayed(updatePlaybackPositionRunnable, UPDATE_PLAYBACK_POSITION_PERIOD)
        }
    }

    private fun updatePlaybackPositionActions() {
        val isVideoAvailable = !currentItem?.video.isNullOrEmpty()
        if (!isVideoAvailable) {
            binding.vvsbvViewMultimediaVideoTimeBar.gone()
            binding.vvssvViewMultimediaVideoTimeBarSmall.gone()
            return
        }
        val videoPlaybackPosition = viewVideoPlaybackController?.getCurrentPosition() ?: 0
        binding.vvssvViewMultimediaVideoTimeBarSmall.setPosition(videoPlaybackPosition)
        binding.vvsbvViewMultimediaVideoTimeBar.setPosition(videoPlaybackPosition)

        val videoDuration = viewVideoPlaybackController?.getDuration()
        if (videoDuration != this.videoDuration) {
            this.videoDuration = videoDuration
            setupTimelineDurations(videoDuration)
        }

        updatePlaybackPosition()
    }

    private fun updateTimelineVisibility(duration: Long?) {
        if (duration == null) return
        val isVideoAvailable = !currentItem?.video.isNullOrEmpty() && currentItem?.isAvailable.isTrue()
        binding.vvssvViewMultimediaVideoTimeBarSmall.isVisible =
            duration >= MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY && isVideoAvailable
    }

    private fun handleStopPlayer() {
        val info = ViewVideoPlayerInfoModel(isVolumeEnabled = false)
        viewVideoPlaybackController?.setVideoData(info)
        viewVideoPlaybackController?.onFragmentStopped()
        disableDisplayAlwaysOn()
    }

    private fun stopPositionUpdate() = playbackPositionHandler?.removeCallbacksAndMessages(null)
    private fun stopDoubleTapHandler() = doubleTapHandler?.removeCallbacksAndMessages(null)
    private fun stopLoaderHandler() = loaderHandler?.removeCallbacksAndMessages(null)

    private fun showLoaderForPlaybackState() {
        stopLoaderHandler()
        if (seekVideoController?.isVisible().isTrue() || !isVideoNeedToPlay || !isVisible || !isResumed) return
        loaderHandler?.postDelayed({ setProgressState(needShow = true) }, LOADER_SHOW_DELAY)
    }

    private fun enableDisplayAlwaysOn() = (requireActivity() as MeeraAct).keepScreenOnEnable()
    private fun disableDisplayAlwaysOn() = (requireActivity() as MeeraAct).keepScreenOnDisable()

    private fun initContentData() {
        val args = arguments ?: run {
            activity?.onBackPressed()
            return
        }
        currentItem = args.getParcelable(ARG_MULTIMEDIA_ITEM_ASSET_DATA)
    }
}
