package com.numplates.nomera3.presentation.view.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.AudioManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.isVisibleToUser
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.AUDIO_FEED_HELPER_VIEW_TAG
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.ViewHolderAudio
import com.numplates.nomera3.modules.peoples.ui.content.holder.BloggerMediaContentListHolder
import com.numplates.nomera3.modules.peoples.ui.utils.BloggerVideoPlayHandler
import com.numplates.nomera3.modules.peoples.ui.utils.ResetPaginationPageHandler
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import io.reactivex.disposables.Disposable
import timber.log.Timber
import kotlin.math.abs

private const val POSITION_FOR_MEDIA = 0L
class PeopleRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var scrollEnabled = true
    var mediaObjects = mutableListOf<MediaObject>()
    var audioFeedHelper: AudioFeedHelper? = null

    private var thumbnail: ImageView? = null
    private var viewHolderParent: View? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var control: CustomControlView? = null
    private var staticDurationView: View? = null
    private var isSilentModeDisposable: Disposable? = null
    private var hldr: BloggerVideoPlayHandler? = null
    private var rootContainer: CardView? = null
    private var videoProgressBar: ProgressBar? = null
    private var firstVideo = true
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var innerPlayPosition = -1
    private var isVideoViewAdded = false
    private var currentVideo = String.empty()

    /**
     * Контролирует состояние воспроизведения
     */
    private var volumeState: VolumeState? = null

    private val positionsMap = mutableMapOf<String, Long>()
    private val volumeListeners: MutableSet<() -> Unit> = mutableSetOf()

    private var adapterDataObserver: FeedRecyclerVideoDataObserver? = FeedRecyclerVideoDataObserver(
        recyclerView = this,
        tryTriggerVideoPlay = ::updateVideoPlayPosition,
        isCurrentPlayPositionAffected = ::checkIfCurrentPlayPositionMoreThanStartDiff
    )

    init {
        initExoPlayer()
    }

    override fun smoothScrollBy(dx: Int, dy: Int) {
        if (scrollEnabled) {
            super.smoothScrollBy(dx, dy)
        }
    }

    fun playVideo(isEndOfList: Boolean) {
        changeControlViewSoundIconVisibility()
        val targetPosition = getTargetPositionForVideo(isEndOfList) ?: return
        if (targetPosition == playPosition) return
        val holder = findViewHolderForAdapterPosition(targetPosition)
        if (holder != null && holder is BloggerMediaContentListHolder) {
            handleBloggerMediaContentList(
                holder = holder,
                targetPosition = targetPosition
            )
        } else {
            showPreview()
            removeAllViewsFromContainer()
            setStaticDurationViewVisible(true)
            playPosition = -1
        }
    }

    fun showPreview() {
        videoSurfaceView?.alpha = 0f
    }

    fun hidePreview() {
        videoSurfaceView?.player = videoPlayer
        videoSurfaceView?.alpha = 1f
    }

    fun addVolumeSwitchListener(listener: () -> Unit) = volumeListeners.add(listener)

    fun removeVolumeSwitchListener(listener: () -> Unit) = volumeListeners.remove(listener)

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }
        viewHolderParent = null
        resetVideoDuration()
        isSilentModeDisposable?.dispose()
    }

    fun onStop() {
        videoPlayer?.playWhenReady = false
        adapterDataObserver?.unregisterObserver()
    }

    fun onStart() {
        if (playPosition != -1) videoPlayer?.playWhenReady = true else playVideo(false)
        adapterDataObserver?.registerObserver()
    }

    fun onDestroyView() {
        isSilentModeDisposable?.dispose()
        adapterDataObserver?.unregisterObserver()
        adapterDataObserver = null
    }

    fun turnOffAudioOfVideo() {
        setVolumeControl(VolumeState.OFF)
        changeControlViewSoundIconVisibility()
        showControlViewDurationContainer()
    }

    fun isPlayingVideo(url: String): Boolean {
        return if (currentVideo == url) {
            videoPlayer?.playWhenReady == true && videoPlayer?.playbackState == Player.STATE_READY
        } else {
            false
        }
    }

    fun resetStateAndPlayVideo(
        innerPosition: Int,
        rootPosition: Int
    ) {
        val currentHolder = hldr ?: return
        val targetPosition = getTargetPositionForVideo(!canScrollVertically(1))
        if (innerPlayPosition == innerPosition) return
        if (rootPosition != targetPosition) return
        clearForDetachedView(currentHolder)
        resetVideoSurfaceView()
        this.innerPlayPosition = innerPosition
        tryToPlayVideo()
    }

    fun getPlayingVideoPosition() = videoPlayer?.currentPosition ?: 0

    fun resetHorizontalListPageByPosition(position: Int) {
        val holder = findViewHolderForAdapterPosition(position)
        if (holder !is ResetPaginationPageHandler) return
        holder.resetCurrentPage()
    }

    private fun initVideoSurfaceViewAndStartVideo(holder: BloggerVideoPlayHandler) {
        val mediaUrl = holder.getVideoUrlString()
        if (mediaUrl.isNullOrEmpty()) return
        initControls()
        setVideoDataByHolder(holder)
        setStaticVideoDuration(holder.getStaticDurationView())
        showPreview()
        startVideo(mediaUrl)
    }

    private fun setStaticVideoDuration(durationView: View) {
        this.staticDurationView = durationView
    }

    private fun resetVideoDuration() {
        this.staticDurationView = null
    }

    private fun setVideoDataByHolder(holder: BloggerVideoPlayHandler) {
        videoSurfaceView = holder.getPlayerView()
        videoSurfaceView?.player = videoPlayer
        thumbnail = holder.getThumbnail()
        rootContainer = holder.getRoot()
        viewHolderParent = holder.getItemView()
        currentVideo = holder.getVideoUrlString() ?: String.empty()
    }

    private fun handleBloggerMediaContentList(
        holder: BloggerMediaContentListHolder,
        targetPosition: Int
    ) {
        if (firstVideo) {
            firstVideo = false
            return
        }
        val innerLayoutManager = holder.getRecyclerView().layoutManager as LinearLayoutManager
        val innerFirstHolder = holder.getRecyclerView().findViewHolderForAdapterPosition(
            innerLayoutManager.findFirstCompletelyVisibleItemPosition()
        )
        if (innerFirstHolder is BloggerVideoPlayHandler) {
            hldr = innerFirstHolder
            playPosition = targetPosition
            resetVideoSurfaceView()
            setStaticDurationViewVisible(true)
            initVideoSurfaceViewAndStartVideo(innerFirstHolder)
            constrainProgressBar()
        } else {
            showPreview()
            removeAllViewsFromContainer()
            setStaticDurationViewVisible(true)
            playPosition = -1
        }
    }

    private fun resetVideoSurfaceView() {
        removeAllViewsFromContainer()
        videoSurfaceView?.player = null
        videoSurfaceView?.alpha = 0f
    }

    private fun getTargetPositionForVideo(isEndOfList: Boolean): Int? {
        val linearLayoutManager = layoutManager as? LinearLayoutManager ?: return null
        val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastPosition = linearLayoutManager.findLastVisibleItemPosition()
        if (isEndOfList) return lastPosition
        val childInScreenCenterPosition = getChildInScreenCenterPosition(linearLayoutManager)
        if (childInScreenCenterPosition != -1) return childInScreenCenterPosition
        if (firstPosition < 0 || lastPosition < 0) return null
        return if (firstPosition != lastPosition) {
            val startPositionVideoHeight = getVisibleVideoSurfaceHeight(
                linearLayoutManager = linearLayoutManager,
                playPosition = firstPosition
            )
            val endPositionVideoHeight = getVisibleVideoSurfaceHeight(
                linearLayoutManager = linearLayoutManager,
                playPosition = lastPosition
            )
            if (startPositionVideoHeight > endPositionVideoHeight) firstPosition else lastPosition
        } else {
            firstPosition
        }
    }

    private fun clearCacheForCurrentVideo() {
        Act.simpleCache?.removeResource(currentVideo)
    }

    private fun initExoPlayer() {
        initSurfaceHeight()
        initPlayer()
        initListeners()
        initPlayerListeners()
        initProgressBar()
        initSilentMode()
    }

    private fun initSurfaceHeight() {
        getDisplaySize(context)?.apply {
            videoSurfaceDefaultHeight = x
            screenDefaultHeight = y
        }
    }

    private fun initProgressBar() {
        val context = context ?: return
        videoProgressBar = ProgressBar(context)
        videoProgressBar?.indeterminateDrawable?.setColorFilter(
            Color.WHITE,
            android.graphics.PorterDuff.Mode.MULTIPLY
        )
    }

    private fun initPlayer() {
        val ctx = context ?: return
        val trackSelector = DefaultTrackSelector(ctx.applicationContext)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        videoPlayer = SimpleExoPlayer
            .Builder(context.applicationContext, DefaultRenderersFactory(context))
            .setTrackSelector(trackSelector)
            .build()

        videoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
    }

    private fun initControls() {
        val ctx = context ?: return
        control = CustomControlView(ctx, R.layout.blogger_media_duration_controller)
        control?.player = videoPlayer
        val volumeState = VolumeState.OFF
        setVolumeControl(volumeState)
    }

    private fun initListeners() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                audioFeedHelper?.onScrolled()
                super.onScrolled(recyclerView, dx, dy)
                if (abs(dy) > 50 && recyclerView.canScrollVertically(-dx)) return
                if (thumbnail != null) thumbnail?.visible()
                tryToPlayVideo()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    SCROLL_STATE_IDLE -> {
                        if (thumbnail != null) thumbnail?.visible()
                        tryToPlayVideo()
                    }
                    SCROLL_STATE_SETTLING,
                    SCROLL_STATE_DRAGGING -> Unit
                }
            }
        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG) {
                    val position = getChildAdapterPosition(view)
                    if (position == -1) return
                    val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                    if (holder is ViewHolderAudio) holder.subscribe()
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                Timber.d("PeopleRecyclerView child view detached from window!")
                if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG) {
                    val position = getChildAdapterPosition(view)
                    if (position == -1) return
                    val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                    if (holder is ViewHolderAudio) holder.unSubscribe()
                }
                if (viewHolderParent != null && viewHolderParent == view) {
                    val position = getChildAdapterPosition(view)
                    val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                    if (holder is BloggerMediaContentListHolder) {
                        val innerLayoutManager = (holder.getRecyclerView().layoutManager as LinearLayoutManager)
                        val innerHolder = holder.getRecyclerView().findViewHolderForAdapterPosition(
                            innerLayoutManager.findFirstVisibleItemPosition()
                        ) as? BloggerVideoPlayHandler ?: return
                        clearForDetachedView(innerHolder)
                    }
                }
            }
        })

    }

    private fun initPlayerListeners() {
        videoPlayer?.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                Timber.d("Player.EventListener onTimelineChanged")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                keepScreenOn = playWhenReady
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Timber.d("onPlayerStateChanged: Buffering video.")
                    }
                    Player.STATE_READY -> {
                        videoSurfaceView?.alpha = 1f
                        if (!isVideoViewAdded) {
                            Timber.d("onPlayerStateChanged: Ready to play.")
                            addVideoView()
                            constraintCounter()
                        }
                        hideProgressBar()
                    }
                    else -> Unit
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                clearCacheForCurrentVideo()
                saveCurrentPosition()
            }
        })
    }

    private fun initSilentMode() {
        val activity = getActivity() as? Act
        isSilentModeDisposable = activity
            ?.ringerModePublishSubject
            ?.subscribe {
                if (isVideoViewAdded) {
                    val newVolumeState = if (it == AudioManager.RINGER_MODE_NORMAL) VolumeState.ON else VolumeState.OFF
                    setVolumeControl(newVolumeState)
                }
            }
    }

    private fun isViewInRecyclerViewCenter(view: View?, recyclerViewCenterY: Int): Boolean {
        if (view == null) return false
        val childViewTopY = view.getYPositionOnScreen()
        val childViewBottomY = childViewTopY + view.height
        return childViewTopY <= childViewBottomY && recyclerViewCenterY in childViewTopY..childViewBottomY
    }

    /**
     * Поиск view расположенной в центре
     */
    private fun getChildInScreenCenterPosition(linearLayoutManager: LinearLayoutManager): Int {
        val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
        if (firstVisibleItemPosition < 0 || lastVisibleItemPosition < 0) return -1
        if (firstVisibleItemPosition > lastVisibleItemPosition) return -1
        val recyclerViewCenterY = getYPositionOnScreen() + height / 2
        for (visibleItemPosition in firstVisibleItemPosition..lastVisibleItemPosition) {
            val visibleChild = findViewHolderForAdapterPosition(visibleItemPosition)
            val isViewInRecyclerViewCenter = isViewInRecyclerViewCenter(
                view = visibleChild?.itemView,
                recyclerViewCenterY = recyclerViewCenterY
            )
            if (isViewInRecyclerViewCenter) return visibleItemPosition
        }
        return -1
    }

    private fun startVideo(media: String) {
        val dataSource = DefaultDataSource.Factory(context)
        val cacheDataSource = CacheDataSource.Factory()
                .setCache(Act.simpleCache!!)
                .setUpstreamDataSourceFactory(dataSource)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(cacheDataSource)
            .createMediaSource(MediaItem.fromUri(media))
        videoPlayer?.prepare(mediaSource, true, false)
        videoPlayer?.seekTo(POSITION_FOR_MEDIA)
        fixSurfaceViewRefreshIssue()
        videoPlayer?.playWhenReady = true
    }

    private fun fixSurfaceViewRefreshIssue() {
        (videoSurfaceView?.videoSurfaceView as? SurfaceView)?.apply {
            holder?.setFormat(PixelFormat.TRANSPARENT)
            holder?.setFormat(PixelFormat.OPAQUE)
            bringToFront()
        }
    }

    private fun removeAllViewsFromContainer() {
        removeVideoView(videoSurfaceView)
        removeCommonView(videoProgressBar)
        removeCommonView(control)
    }

    private fun constraintCounter() {
        val params = FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM
        rootContainer?.addView(control, params)
        control?.requestFocus()
        changeControlViewSoundIconVisibility()
        control?.iconContainer?.visible()
        setStaticDurationViewVisible(false)
    }

    private fun setStaticDurationViewVisible(isVisible: Boolean) {
        staticDurationView?.isVisible = isVisible
    }

    private fun constrainProgressBar() {
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER
        rootContainer?.addView(videoProgressBar, lp)
        videoProgressBar?.visible()
        videoProgressBar?.requestFocus()
    }

    private fun hideProgressBar() {
        videoProgressBar?.gone()
    }

    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state == VolumeState.OFF) {
            videoPlayer?.volume = 0f
        } else if (state == VolumeState.ON) {
            volumeListeners.forEach { it() }
            videoPlayer?.volume = 1f
        }
    }

    private fun changeControlViewSoundIconVisibility() {
        when (volumeState) {
            VolumeState.ON -> control?.setSoundOn()
            VolumeState.OFF -> control?.setSoundOff()
            null -> control?.setSoundOff()
        }
    }

    private fun showControlViewDurationContainer() {
        if (control?.iconContainer?.alpha != 1f) {
            control?.iconContainer?.animate()
                ?.alpha(1f)
                ?.setDuration(750L)
                ?.setListener(null)
                ?.start()
        }
    }

    private fun addVideoView() {
        isVideoViewAdded = true
        videoSurfaceView?.player = videoPlayer
        videoSurfaceView?.alpha = 1f
        videoSurfaceView?.requestFocus()
    }

    private fun clearForDetachedView(videoViewHolder: BloggerVideoPlayHandler) {
        if (isVideoViewAdded) {
            removeVideoView(videoViewHolder.getPlayerView())
            removeCommonView(videoProgressBar)
            removeCommonView(control)
            resetPositionAndShowPreview()
            setStaticDurationViewVisible(true)
        }
    }

    private fun resetPositionAndShowPreview() {
        playPosition = -1
        videoSurfaceView?.alpha = 0f
        thumbnail?.visibility = View.VISIBLE
    }

    private fun removeCommonView(view: View?) {
        view?.parent ?: return
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
    }

    private fun removeVideoView(videoView: PlayerView?) {
        if (!isVideoViewAdded) return
        showThumbnail()
        resetVideoView(videoView)
        saveCurrentPosition()
        setIsVideoViewAdded(false)
    }

    private fun setIsVideoViewAdded(isAdded: Boolean) {
        isVideoViewAdded = isAdded
    }

    private fun resetVideoView(videoView: PlayerView?) {
        videoView?.alpha = 0f
        videoPlayer?.playWhenReady = false
        videoView?.player = null
    }

    private fun showThumbnail() {
        thumbnail?.visible()
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    private fun getVisibleVideoSurfaceHeight(
        linearLayoutManager: LinearLayoutManager,
        playPosition: Int
    ): Int {
        val at = playPosition - linearLayoutManager.findFirstVisibleItemPosition()
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) location[1] + videoSurfaceDefaultHeight else screenDefaultHeight - location[1]
    }

    private fun saveCurrentPosition() {
        positionsMap[currentVideo] = videoPlayer?.currentPosition ?: 0
    }

    private fun updateVideoPlayPosition(playPositionDifference: Int) {
        if (playPosition != -1) playPosition += playPositionDifference
        tryToPlayVideo()
    }

    private fun checkIfCurrentPlayPositionMoreThanStartDiff(startPositionOfDiff: Int): Boolean {
        return playPosition >= startPositionOfDiff
    }

    private fun tryToPlayVideo() {
        if (isVisibleToUser()) playVideo(!canScrollVertically(1))
    }

    private fun View.getYPositionOnScreen(): Int {
        val locationOnScreen = IntArray(2)
        this.getLocationOnScreen(locationOnScreen)
        return locationOnScreen[1]
    }

    private fun getDisplaySize(ctx: Context?): Point? {
        if (ctx == null) return null
        val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point
    }

    enum class VolumeState { ON, OFF }

    data class MediaObject(
        val title: String,
        val media_url: String,
        val thumbnail: String,
        val description: String
    )
}
