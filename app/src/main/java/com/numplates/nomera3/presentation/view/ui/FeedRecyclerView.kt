package com.numplates.nomera3.presentation.view.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.isVisibleToUser
import com.numplates.nomera3.Act
import com.numplates.nomera3.Act.Companion.simpleCache
import com.numplates.nomera3.modules.baseCore.helper.AUDIO_FEED_HELPER_VIEW_TAG
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.ViewHolderAudio
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.viewholder.LongIndicatorViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MultimediaPostHolder
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.widgets.VideoRetryView
import io.reactivex.disposables.Disposable
import kotlin.math.abs

const val FEED_POSITION_FOR_MEDIA = 0L
const val POST_BOTTOM_PART_VISIBILITY_THRESHOLD = 0.05

class FeedRecyclerView : RecyclerView {

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    /**
     * TODO ТехДолг
     * https://nomera.atlassian.net/browse/BR-14862
     */
    var scrollEnabled = true
    var audioFeedHelper: AudioFeedHelper? = null
    var currentRoadMode: MainRoadMode? = null

    private var retryView: VideoRetryView? = null
    private var isSilentModeDisposable: Disposable? = null
    private var hldr: VideoViewHolder? = null
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var playPositionInMultimediaPost = -1
    private var currentVideo = ""
    private var onViewPagerSwipeStateChangeListener: MainPostRoadsFragment.OnViewPagerSwipeStateChangeListener? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    /**
     * Контролирует состояние воспроизведения
     */

    private val positionsMap = mutableMapOf<String, Long>()

    private var adapterDataObserver: FeedRecyclerVideoDataObserver? = FeedRecyclerVideoDataObserver(
        recyclerView = this,
        tryTriggerVideoPlay = ::updateVideoPlayPosition,
        isCurrentPlayPositionAffected = ::checkIfCurrentPlayPositionMoreThanStartDiff
    )

    override fun smoothScrollBy(dx: Int, dy: Int) {
        if (scrollEnabled) {
            super.smoothScrollBy(dx, dy)
        }
    }

    fun setOnViewPagerSwipeStateChangeListener(
        onViewPagerSwipeStateChangeListener: MainPostRoadsFragment.OnViewPagerSwipeStateChangeListener?
    ) {
        this.onViewPagerSwipeStateChangeListener = onViewPagerSwipeStateChangeListener
    }

    fun setVolumeStateCallback(
        volumeStateCallback: VolumeStateCallback
    ) {
        this.volumeStateCallback = volumeStateCallback
    }

    fun getCurrentMediaId(): String? {
        val currentHolder = hldr
        if (currentHolder !is MultimediaPostHolder) return null
        return currentHolder.getCurrentMedia()?.id
    }

    fun playVideo(isEndOfList: Boolean, currentVideoPosition: Long? = null) {
        val targetPosition = getTargetPositionForVideo(isEndOfList) ?: return
        val isPlaying = getCurrentVideoPlayer()?.playWhenReady.isTrue()
        val holder = findViewHolderForAdapterPosition(targetPosition)
        if (holder != null && holder is VideoViewHolder) {
            val multimediaHolder = holder as? MultimediaPostHolder?
            val isSelectedCurrentPositionInPager = if (multimediaHolder != null) {
                multimediaHolder.getCurrentMediaPosition() == playPositionInMultimediaPost
            } else {
                true
            }
            if (targetPosition == playPosition && isSelectedCurrentPositionInPager && isPlaying) return
            val oldHolder = hldr
            hldr = holder
            holder.onShowPostClicked = { playVideo(false, currentVideoPosition) }
            oldHolder?.stopPlayingVideo()
            if (!holder.needToPlay()) return
            playPosition = targetPosition
            if (holder is MultimediaPostHolder) {
                playPositionInMultimediaPost = holder.getCurrentMediaPosition()
            }
            initVideoSurfaceViewAndStartVideo(holder, currentVideoPosition)
        } else {
            playPosition = -1
            playPositionInMultimediaPost = -1
        }
    }

    fun getVisiblePositions(): Pair<Int, Int>? {
        val linearLayoutManager = layoutManager as? LinearLayoutManager ?: return null
        val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastPosition = linearLayoutManager.findLastVisibleItemPosition()
        return Pair(firstPosition, lastPosition)
    }

    fun clear() {
        isSilentModeDisposable?.dispose()
    }

    fun onStopIfNeeded() {
        val currentHolder = hldr ?: return
        val needToPlay = currentHolder.needToPlay()
        if (!needToPlay) {
            onStop()
        }
    }

    fun onStop() {
        hldr?.stopPlayingVideo()
        adapterDataObserver?.unregisterObserver()
    }

    fun onStart(lastPostMediaViewInfo: PostMediaViewInfo?) {
        if (currentRoadMode == MainRoadMode.MAP) return
        val currentVideoPlaybackPosition = lastPostMediaViewInfo?.lastVideoPlaybackPosition
        playVideo(false, currentVideoPlaybackPosition)
        adapterDataObserver?.registerObserver()
    }

    fun forcePlay() {
        playVideo(false)
    }

    fun onDestroyView() {
        audioFeedHelper?.releasePlayer()
        isSilentModeDisposable?.dispose()
        adapterDataObserver = null
    }

    fun turnOffAudioOfVideo() {
        volumeStateCallback?.setVolumeState(VolumeState.OFF)
    }

    fun isPlayingVideo(url: String): Boolean {
        return if (currentVideo == url) {
            getCurrentVideoPlayer()?.playWhenReady == true && getCurrentVideoPlayer()?.playbackState == Player.STATE_READY
        } else {
            false
        }
    }

    fun getPlayingVideoPosition() = getCurrentVideoPlayer()?.currentPosition ?: 0
    fun getPlayingVideoDuration() = getCurrentVideoPlayer()?.duration ?: 0

    fun isVolumeEnabled() = volumeStateCallback?.getVolumeState() == VolumeState.ON

    private fun getCurrentVideoPlayer() = hldr?.getVideoPlayerView()?.player as? ExoPlayer?

    private fun initVideoSurfaceViewAndStartVideo(holder: VideoViewHolder, currentVideoPosition: Long?) {
        val mediaUrl = holder.getVideoUrlString()
        if (mediaUrl.isNullOrEmpty()) return
        currentVideo = mediaUrl
        startVideo(currentVideoPosition)
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
        simpleCache?.removeResource(currentVideo)
    }

    private fun init() {
        getDisplaySize(context)?.apply {
            videoSurfaceDefaultHeight = x
            screenDefaultHeight = y
        }
        initRetryView()
        initListeners()
        initSilentMode()
    }

    private fun initRetryView() {
        val ctx = context ?: return
        retryView = VideoRetryView(ctx.applicationContext)
        retryView?.setOnClickListener {
            if (hldr?.needToPlay() == false) {
                removeCommonView(retryView)
                return@setOnClickListener
            }
            clearCacheForCurrentVideo()
            startVideo()
        }
    }

    private fun getMultipleMediaHoldersCoords() {
        val layoutManager = (layoutManager as? LinearLayoutManager?) ?: return
        val firstPosition = layoutManager.findFirstVisibleItemPosition()
        val lastPosition = layoutManager.findLastVisibleItemPosition()

        val containerRects = arrayListOf<Rect>()

        for (position in firstPosition..lastPosition) {
            val holder = findViewHolderForAdapterPosition(position) as? MultimediaPostHolder?
            holder?.let {
                val container = it.getMediaContainer() ?: return
                val containerRect = Rect()
                container.getGlobalVisibleRect(containerRect)

                containerRects.add(containerRect)
            }
        }

        onViewPagerSwipeStateChangeListener?.onMultimediaPostsCoordsChanged(containerRects)
    }

    private fun initListeners() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                audioFeedHelper?.onScrolled()
                super.onScrolled(recyclerView, dx, dy)
                expandMediaIndicatorAction(true)
                if (abs(dy) > 50 && recyclerView.canScrollVertically(-1)) return
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    SCROLL_STATE_IDLE -> {
                        tryToPlayVideo()
                        getMultipleMediaHoldersCoords()
                    }
                }
                expandMediaIndicatorAction(recyclerStopped = newState == SCROLL_STATE_IDLE)
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
                val position = getChildAdapterPosition(view)
                val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                if (holder is VideoViewHolder) holder.initPlayer()
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG) {
                    val position = getChildAdapterPosition(view)
                    if (position == -1) return
                    val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                    if (holder is ViewHolderAudio) holder.unSubscribe()
                }
                val position = getChildAdapterPosition(view)
                val holder: ViewHolder? = findViewHolderForAdapterPosition(position)
                if (holder is VideoViewHolder) clearForDetachedView(holder)
            }
        })
    }

    fun expandMediaIndicatorAction(recyclerStopped: Boolean, showInstantly: Boolean = false) {
        val linearLayoutManager = layoutManager as? LinearLayoutManager ?: return
        val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastPosition = linearLayoutManager.findLastVisibleItemPosition()
        if (firstPosition < 0 || lastPosition < 0 || firstPosition > lastPosition) return
        for (position in firstPosition..lastPosition) {
            val holder = findViewHolderForAdapterPosition(position) as? LongIndicatorViewHolder
            holder?.let {
                when {
                    showInstantly -> holder.showExpandMediaIndicator()
                    isHolderOutOfScreen(holder) -> holder.hideLongIndicator()
                    recyclerStopped -> controlMediaExpandDelayedAction(holder)
                    else -> holder.stopDelayedShow()
                }
            }
        }
    }

    private fun controlMediaExpandDelayedAction(holder: LongIndicatorViewHolder) {
        if (isHolderBottomPartVisible(holder)) {
            holder.startDelayedShow()
        } else {
            holder.stopDelayedShow()
        }
    }

    private fun isHolderOutOfScreen(holder: LongIndicatorViewHolder): Boolean {
        val mediaBottom = getBottomLocation(holder.getLongMediaContainer() ?: return true)
        val recyclerViewTop = top
        val recyclerViewBottom = bottom
        val screenHeight = resources.displayMetrics.heightPixels
        val visibleScreenThreshold = screenHeight * POST_BOTTOM_PART_VISIBILITY_THRESHOLD
        return (mediaBottom - recyclerViewBottom > visibleScreenThreshold)  || (mediaBottom < recyclerViewTop)
    }

    private fun getBottomLocation(longMediaContainer: View): Int {
        val location = IntArray(2)
        longMediaContainer.getLocationOnScreen(location)
        val y = location[1]
        val viewHeight = longMediaContainer.height

        val bottomY = y + viewHeight

        return bottomY
    }



    private fun isHolderBottomPartVisible(holder: LongIndicatorViewHolder): Boolean {
        val mediaBottom = getBottomLocation(holder.getLongMediaContainer() ?: return true)
        val screenHeight = resources.displayMetrics.heightPixels
        val visibleScreenThreshold = screenHeight * POST_BOTTOM_PART_VISIBILITY_THRESHOLD
        val recyclerViewTop = top
        val actionBarOffset = holder.getContentBarHeight()
        val recyclerViewBottomWithContentBarOffset = bottom + actionBarOffset
        return mediaBottom in recyclerViewTop..recyclerViewBottomWithContentBarOffset
            && (mediaBottom - recyclerViewTop) >= visibleScreenThreshold
    }

    //TODO multimedia перенести слушатель в FeedViewModel
    private fun initSilentMode() {
        val activity = getActivity() as? Act
        isSilentModeDisposable = activity
            ?.ringerModePublishSubject
            ?.subscribe {}
    }

    private fun isViewInRecyclerViewCenter(view: View?, recyclerViewCenterY: Int): Boolean {
        if (view == null) return false
        val childViewTopY = view.getYPositionOnScreen()
        val childViewBottomY = childViewTopY + view.height
        return childViewTopY <= childViewBottomY && recyclerViewCenterY in childViewTopY..childViewBottomY
    }

    /**
     * Поиск view расположенной в центре [FeedRecyclerView].
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

    private fun startVideo(currentVideoPosition: Long? = null) {
        hldr?.startPlayingVideo(position = currentVideoPosition ?: FEED_POSITION_FOR_MEDIA)
    }

    private fun clearForDetachedView(videoViewHolder: VideoViewHolder) {
        removeVideoView(videoViewHolder)
        videoViewHolder.detachPlayer()
    }

    private fun removeVideoView(videoViewHolder: VideoViewHolder) {
        videoViewHolder.stopPlayingVideo()
        saveCurrentPosition()
    }

    private fun removeCommonView(view: View?) {
        view?.parent ?: return
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
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
        positionsMap[currentVideo] = getCurrentVideoPlayer()?.currentPosition ?: 0
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
}
