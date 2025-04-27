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
import com.meera.core.extensions.dp
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.isVisibleToUser
import com.numplates.nomera3.Act
import com.numplates.nomera3.Act.Companion.simpleCache
import com.numplates.nomera3.modules.baseCore.helper.AUDIO_FEED_HELPER_VIEW_TAG
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.ViewHolderAudio
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.viewholder.LongIndicatorViewHolder
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraMultimediaPostHolder
import com.numplates.nomera3.modules.redesign.fragments.main.MainRoadFragment
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.widgets.VideoRetryView
import io.reactivex.disposables.Disposable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//TODO ROAD_FIX
//const val FEED_POSITION_FOR_MEDIA = 0L
//const val POST_BOTTOM_PART_VISIBILITY_THRESHOLD = 0.05
const val ITEM_HORIZONTAL_PADDING = 16
const val POST_MINIMUM_PERCENTAGE_VISIBILITY_FOR_STOP_PLAYING = 30
const val MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL = 1
const val MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL = 4

class MeeraFeedRecyclerView : RecyclerView {

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

    private var audioFeedHelper: AudioFeedHelper? = null
    private var retryView: VideoRetryView? = null
    private var isSilentModeDisposable: Disposable? = null
    private var hldr: VideoViewHolder? = null
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var playPositionInMultimediaPost = -1
    private var currentVideo = ""
    private var onViewPagerSwipeStateChangeListener: MainRoadFragment.OnViewPagerSwipeStateChangeListener? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    private var expandMediaScrollListener : OnScrollListener? = null
    private var onChildAttachStateChangeListener: OnChildAttachStateChangeListener? = null
    private var showPostClickedListener: (()->Unit)? = null
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
        onViewPagerSwipeStateChangeListener: MainRoadFragment.OnViewPagerSwipeStateChangeListener?
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
        if (currentHolder !is MeeraMultimediaPostHolder) return null
        return currentHolder.getCurrentMedia()?.id
    }

    fun playVideo(isEndOfList: Boolean, currentVideoPosition: Long? = null) {
        val targetPosition = getTargetPositionForVideo(isEndOfList) ?: return
        val currentPlayer = getCurrentVideoPlayer()
        val isPlaying = currentPlayer?.playWhenReady.isTrue() && currentPlayer?.playbackState != Player.STATE_IDLE
        val holder = findViewHolderForAdapterPosition(targetPosition)
        if (holder != null && holder is VideoViewHolder) {
            val multimediaHolder = holder as? MeeraMultimediaPostHolder?
            val isSelectedCurrentPositionInPager = if (multimediaHolder != null) {
                multimediaHolder.getCurrentMediaPosition() == playPositionInMultimediaPost
            } else {
                true
            }
            if (targetPosition == playPosition && isSelectedCurrentPositionInPager && isPlaying) return
            val oldHolder = hldr
            hldr = holder
            showPostClickedListener = { playVideo(false, currentVideoPosition) }.also {
                holder.onShowPostClicked = it
            }
            oldHolder?.stopPlayingVideo()
            if (!holder.needToPlay()) return
            playPosition = targetPosition
            if (holder is MeeraMultimediaPostHolder) {
                playPositionInMultimediaPost = holder.getCurrentMediaPosition()
            }
            initVideoSurfaceViewAndStartVideo(holder, currentVideoPosition)
        } else {
            val playingHolder = findViewHolderForAdapterPosition(playPosition) as? VideoViewHolder?
            val isUrlEquals = playingHolder?.getVideoUrlString() == hldr?.getVideoUrlString()
            val playPositionVisibilityPercent = calculateVisibilityPercentageOfView(playPosition)
            if (playPositionVisibilityPercent < POST_MINIMUM_PERCENTAGE_VISIBILITY_FOR_STOP_PLAYING
                || playingHolder == null
                || !isUrlEquals
            ) {
                hldr?.stopPlayingVideo()
            } else {
                if (isPlaying) return
                hldr?.startPlayingVideo()
            }
        }
    }

    fun getVisiblePositions(): Pair<Int, Int>? {
        val linearLayoutManager = layoutManager as? LinearLayoutManager ?: return null
        val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val lastPosition = linearLayoutManager.findLastVisibleItemPosition()
        return Pair(firstPosition, lastPosition)
    }

    fun onStopIfNeeded() {
        val currentHolder = hldr ?: return
        val needToPlay = currentHolder.needToPlay()
        if (!needToPlay) {
            onStop()
        }
    }

    fun onStop(isFromMultimedia: Boolean = false) {
        if (isFromMultimedia) {
            if (hldr is MeeraMultimediaPostHolder) hldr?.stopPlayingVideo()
        } else {
            hldr?.stopPlayingVideo()
        }

        adapterDataObserver?.unregisterObserver()
    }

    fun resetCurrentPlayingHolderIfNeeded(position: Int) {
        if (playPosition != position) return
        playPosition = -1
        hldr?.stopPlayingVideo()?.also { hldr = null }
    }

    fun onStart(lastPostMediaViewInfo: PostMediaViewInfo?) {
        if (NavigationManager.getManager().isMapMode) return
        val currentVideoPlaybackPosition = lastPostMediaViewInfo?.lastVideoPlaybackPosition
        playVideo(false, currentVideoPlaybackPosition)
        adapterDataObserver?.registerObserver()
    }

    fun forcePlay() {
        playVideo(false)
    }

    fun forcePlayFromStart() {
        playVideo(false, 0)
    }

    fun release() {
        recycledViewPool.clear()
        audioFeedHelper?.releasePlayer()
        adapterDataObserver?.onDestroy()
        isSilentModeDisposable?.dispose()
        retryView?.setOnClickListener(null)
        expandMediaScrollListener?.let {
            removeOnScrollListener(it)
            expandMediaScrollListener = null
        }
        onChildAttachStateChangeListener?.let {
            removeOnChildAttachStateChangeListener(it)
            onChildAttachStateChangeListener = null
        }
        hldr?.onShowPostClicked = null
        hldr = null
        retryView = null
        showPostClickedListener = null
        onViewPagerSwipeStateChangeListener = null
        audioFeedHelper = null
        volumeStateCallback = null
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

    private fun getCurrentVideoPlayer(): ExoPlayer? {
        val player = hldr?.getVideoPlayerView()?.player as? ExoPlayer?
        val actualHolder = (findViewHolderForAdapterPosition(playPosition) as? VideoViewHolder?)
        if (actualHolder == null || hldr?.getVideoPlayerView() == actualHolder.getVideoPlayerView()) return player
        hldr = actualHolder
        return hldr?.getVideoPlayerView()?.player as? ExoPlayer?
    }

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

    private fun calculateVisibilityPercentageOfView(position: Int): Int {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return 0
        val view = layoutManager.findViewByPosition(position) ?: return 0

        val rect = Rect()
        getDecoratedBoundsWithMargins(view, rect)

        val parentRect = Rect()
        getGlobalVisibleRect(parentRect)

        val visibleTop = max(rect.top, parentRect.top)
        val visibleBottom = min(rect.bottom, parentRect.bottom)

        val visibleHeight = max(0, visibleBottom - visibleTop)
        val itemHeight = rect.height()

        return if (itemHeight > 0) {
            ((visibleHeight.toFloat() / itemHeight) * 100).toInt()
        } else {
            0
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
        initMaxRecycledPool()
        initRetryView()
        initListeners()
        initSilentMode()
    }

    private fun initMaxRecycledPool() {
        recycledViewPool.setMaxRecycledViews(FeedType.MOMENTS.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.IMAGE_POST.viewType, MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.REPOST.viewType, MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.VIDEO_POST.viewType, MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.VIDEO_REPOST.viewType, MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(
            FeedType.SHIMMER_PLACEHOLDER.viewType,
            MAX_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL
        )
        recycledViewPool.setMaxRecycledViews(
            FeedType.SHIMMER_MOMENTS_PLACEHOLDER.viewType,
            MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL
        )
        recycledViewPool.setMaxRecycledViews(FeedType.PROGRESS.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.ANNOUNCEMENT.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.RATE_US.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.POSTS_VIEWED_ROAD.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(
            FeedType.POSTS_VIEWED_PROFILE.viewType,
            MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL
        )
        recycledViewPool.setMaxRecycledViews(
            FeedType.POSTS_VIEWED_PROFILE_VIP.viewType,
            MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL
        )
        recycledViewPool.setMaxRecycledViews(FeedType.SYNC_CONTACTS.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.REFERRAL.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
        recycledViewPool.setMaxRecycledViews(FeedType.SUGGESTIONS.viewType, MIN_COUNT_OF_MEDIA_VIEW_HOLDERS_POOL)
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
            val holder = findViewHolderForAdapterPosition(position) as? MeeraMultimediaPostHolder?
            holder?.let {
                val container = it.getMediaContainer() ?: return
                val containerRect = Rect()
                container.getGlobalVisibleRect(containerRect)

                containerRect.left += ITEM_HORIZONTAL_PADDING.dp
                containerRect.right -= ITEM_HORIZONTAL_PADDING.dp
                containerRects.add(containerRect)
            }
        }

        onViewPagerSwipeStateChangeListener?.onMultimediaPostsCoordsChanged(containerRects)
    }

    private fun initListeners() {
        expandMediaScrollListener = object : OnScrollListener() {
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
        }.also { listener ->
            addOnScrollListener(listener)
        }

        onChildAttachStateChangeListener = object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val holder: ViewHolder = getChildViewHolder(view) ?: return
                if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG && holder is ViewHolderAudio) {
                    holder.subscribe()
                }
                if (holder is VideoViewHolder) holder.initPlayer()
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                val holder: ViewHolder = getChildViewHolder(view) ?: return
                if (view.tag == AUDIO_FEED_HELPER_VIEW_TAG && holder is ViewHolderAudio) {
                    holder.unSubscribe()
                }
                if (holder is VideoViewHolder) clearForDetachedView(holder)
            }
        }.also { listener->
            addOnChildAttachStateChangeListener(listener)
        }
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

    fun setAudioFeedHelper(audioFeedHelper: AudioFeedHelper?) {
        this.audioFeedHelper = audioFeedHelper
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
        hldr?.startPlayingVideo(position = currentVideoPosition)
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
