package com.meera.core.utils.mediaviewer.viewer.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.meera.core.R
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.createProgressBar
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.extensions.visibleDisAppearAnimate
import com.meera.core.utils.listeners.DoubleClickListener
import com.meera.core.utils.mediaviewer.ImageViewerData
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter
import com.meera.core.utils.mediaviewer.listeners.OnHideToolbar
import com.meera.core.views.VideoControlView


internal class ImagesPagerAdapter : RecyclingPagerAdapter<RecyclingPagerAdapter.ViewHolder>() {

    var onVideoClicked: (ImageViewerData) -> Unit = {}

    val dataList = mutableListOf<ImageViewerData>()
    private val holders = mutableListOf<ViewHolder>()

    var startPosition = 0
    var resourceReady: (() -> Unit?)? = null

    var onTapListener: (() -> Unit?) = {}

    var isZoomable = true
    var isSilentMode = false

    var toolbarAnimator: OnHideToolbar? = null

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) = Unit
    }

    fun initOnPageChangeListener(pager: ViewPager) {
        pager.addOnPageChangeListener(onPageChangeListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, position: Int): RecyclingPagerAdapter.ViewHolder =
            when (viewType) {
                VIEW_TYPE_VIDEO -> VideoViewHolder(parent.inflate(R.layout.video_viewer_pager_layout))
                VIEW_TYPE_VIDEO_NOT_PLAYING -> getHolder(parent)
                else -> getHolder(parent)
            }

    private fun getHolder(parent: ViewGroup): ViewHolder {
        val view = parent.inflate(R.layout.item_media_viewer_pager)
        val pvMediaViewer: PhotoView = view.findViewById(R.id.pv_media_viewer)
        pvMediaViewer.apply {
            isEnabled = true
            isZoomable = this@ImagesPagerAdapter.isZoomable
            setOnViewDragListener { _, _ ->
                setAllowParentInterceptOnEdge(scale == 1.0f)
            }
        }
        return ViewHolder(view).also { holders.add(it) }
    }

    override fun getViewTypeForPosition(position: Int): Int {
        return dataList[position].viewType
    }

    override fun onBindViewHolder(holder: RecyclingPagerAdapter.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(position)
            }
            is VideoViewHolder -> {
                holder.bind(position)
            }
        }
    }

    override fun getItemCount() = dataList.size

    fun isScaled(position: Int): Boolean =
            holders.firstOrNull { it.position == position }?.isScaled ?: false

    fun setData(list: MutableList<ImageViewerData>) {
        dataList.clear()
        dataList.addAll(list)
        holders.clear()
        notifyDataSetChanged()
    }

    fun getImgUrlByPosition(position: Int): String? {
        return try {
            dataList[position].getActualStringUri()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getMediaByPosition(position: Int): ImageViewerData? {
        return try {
            dataList[position]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addData(list: MutableList<ImageViewerData>) {
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun getCurrentImageView(position: Int): ImageView? {
        return holders.firstOrNull { it.position == position }?.imageView
    }

    fun getCurrentData(position: Int): ImageViewerData? {
        return dataList[position]
    }

    internal fun resetScale(position: Int) =
            holders.firstOrNull { it.position == position }?.resetScale()

    fun getSelectedImages(): MutableList<Uri> {
        val res = mutableListOf<Uri>()
        dataList.forEach {
            if (it.isSelected)
                res.add(Uri.parse(it.getActualStringUri()))
        }
        return res
    }

    fun getSelectedData(): MutableList<ImageViewerData> {
        val res = mutableListOf<ImageViewerData>()
        dataList.forEach {
            if (it.isSelected) {
                res.add(it)
            }
        }
        return res
    }

    fun removePhoto(pos: Int) {
        dataList.removeAt(pos)
        notifyDataSetChanged()
    }

    inner class VideoViewHolder(val view: View) : RecyclingPagerAdapter.ViewHolder(view) {

        private val controlView: VideoControlView = view.findViewById(R.id.control_view)
        private val flFastBack: FrameLayout = view.findViewById(R.id.fl_fast_back)
        private val flFastForward: FrameLayout = view.findViewById(R.id.fl_fast_forward)
        private val clControlContainer: ConstraintLayout = view.findViewById(R.id.cl_control_container)
        private val cardView4: CardView = view.findViewById(R.id.cardView4)
        private val flBg: FrameLayout = view.findViewById(R.id.fl_bg)
        private val flTest: ImageView = view.findViewById(R.id.fl_test)


        private var videoPlayer: SimpleExoPlayer? = null
        private var videoView: PlayerView? = null
        private var bindPosition: Int? = null
        private var fastBackHandler = Handler()
        private var isControlShown = true
        private var autoCloseHandler = Handler()
        private var animateHideRunnable = AnimateHideRunnable(flFastBack)

        private val autoCloseTask = Runnable {
            hideControl()
        }

        init {
            videoView = itemView.findViewById(R.id.pv_video_viewer)
            val trackSelector = DefaultTrackSelector(itemView.context)
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd())
            val loadControl = DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                            2 * 1024, 15 * 1024,
                            1024, 1024
                    )
                    .createDefaultLoadControl()
            videoPlayer = SimpleExoPlayer.Builder(itemView.context)
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .build()
            videoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            videoView?.useController = false
            videoView?.player = videoPlayer
            controlView.player = videoPlayer
            initPlayerStateListener()
        }

        fun bind(position: Int) {
            this.bindPosition = position
            itemView.context?.let {
                val videoUrl = dataList[position].getActualStringUri()
                startVideo(videoUrl)
            }

            initDoubleClickListeners()

            initPausePlayClickListeners()
            autoCloseHandler.postDelayed(autoCloseTask, 4000)
            if (isSilentMode) {
                controlView.onSoundButtonClicked()
            }
        }

        private fun initDoubleClickListeners() {
            flFastBack.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    var currentPosition = videoPlayer?.currentPosition ?: 0
                    currentPosition -= 5000
                    if (currentPosition < 0) currentPosition = 0
                    videoPlayer?.seekTo(currentPosition)
                    flFastForward.alpha = 0F
                    flFastBack.visibleAppearAnimate()
                    fastBackHandler.removeCallbacks(animateHideRunnable)
                    animateHideRunnable = AnimateHideRunnable(flFastBack)
                    fastBackHandler.postDelayed(animateHideRunnable, 1000)
                }

                override fun onViewClick() {
                    onControlClicked()
                }
            })

            flFastForward.setOnClickListener(object : DoubleClickListener() {
                override fun onDoubleClick() {
                    var currentPosition = videoPlayer?.currentPosition ?: 0
                    currentPosition += 5000
                    if (currentPosition > videoPlayer?.duration ?: 0) currentPosition = videoPlayer?.duration
                            ?: 0
                    videoPlayer?.seekTo(currentPosition)
                    flFastBack.alpha = 0F
                    flFastForward.visibleAppearAnimate()
                    fastBackHandler.removeCallbacks(animateHideRunnable)
                    animateHideRunnable = AnimateHideRunnable(flFastForward)
                    fastBackHandler.postDelayed(animateHideRunnable, 1000)
                }

                override fun onViewClick() {
                    onControlClicked()
                }
            })

            clControlContainer.setOnClickListener {
                onControlClicked()
            }

        }

        private fun hideControl() {
            if (!isControlShown) return
            controlView.animateHeight(0, 200) {
            }
            cardView4.invisible()
            toolbarAnimator?.hideToolbar()
            flBg.visibleDisAppearAnimate()
            isControlShown = false
        }

        private fun showControl() {
            if (isControlShown) return
            autoCloseHandler.removeCallbacks(autoCloseTask)
            controlView.measure(ViewGroup.LayoutParams.MATCH_PARENT, 58.dp)
            controlView.animateHeight(controlView.measuredHeight, 200) {
                autoCloseHandler.postDelayed(autoCloseTask, 4000)
            }
            cardView4.visibleAppearAnimate()
            toolbarAnimator?.showToolbar()
            flBg.visibleAppearAnimate()
            isControlShown = true
        }

        private fun onControlClicked() {
            if (isControlShown)
                hideControl()
            else showControl()
        }


        private fun initPausePlayClickListeners() {
            flTest.setOnClickListener {
                videoPlayer?.playWhenReady = videoPlayer?.playWhenReady != true
            }
        }

        private fun buildMediaSourceNew(uri: Uri): MediaSource {
            val datasourceFactroy = DefaultDataSourceFactory(itemView.context,
                    Util.getUserAgent(itemView.context,
                            itemView.context.getString(R.string.app_name)))
            return ProgressiveMediaSource.Factory(datasourceFactroy).createMediaSource(uri)
        }

        private fun startVideo(media: String) {
            val mediaSource = buildMediaSourceNew(Uri.parse(media))
            videoPlayer?.prepare(mediaSource, true, false)
            videoPlayer?.playWhenReady = true
        }

        fun releasePlayer() {
            itemView.keepScreenOn = false
            videoPlayer?.release()
        }

        fun pausePlayer() {
            videoPlayer?.playWhenReady = false
        }


        private fun initPlayerStateListener() {
            videoPlayer?.addListener(object : Player.EventListener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) = Unit
                override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) = Unit
                override fun onLoadingChanged(isLoading: Boolean) = Unit
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    itemView.keepScreenOn = playWhenReady
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d("ImagesPagedAdapter", "STATE_BUFFERING")
                        }
                        Player.STATE_ENDED -> {
                            videoPlayer?.seekTo(0)
                            videoPlayer?.playWhenReady = false
                        }
                        Player.STATE_IDLE -> {
                            Log.d("ImagesPagedAdapter", "STATE_IDLE")
                        }
                        Player.STATE_READY -> {
                            Log.d("ImagesPagedAdapter", "STATE_READY")
                            initPausePlayClickListeners()
                            flTest.setVisible(!playWhenReady)
                        }
                        else -> {
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) = Unit
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) = Unit
                override fun onPlayerError(error: PlaybackException) {
                    flTest.loadGlide(R.drawable.ic_video_retry)
                    flTest.setOnClickListener {
                        bindPosition?.let { nonNullPosition ->
                            val videoUrl = dataList[nonNullPosition].getActualStringUri()
                            startVideo(videoUrl)
                        }
                    }
                }

                override fun onPositionDiscontinuity(reason: Int) = Unit
                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) = Unit
                override fun onSeekProcessed() = Unit
            })
        }
    }

    class AnimateShowRunnable(var view: View?): Runnable {
        override fun run() {
            view?.alpha = 1f
        }
    }

    class AnimateHideRunnable(var view: View?): Runnable {
        override fun run() {
            view?.alpha = 0f
        }
    }

    internal inner class ViewHolder(val view: View) : RecyclingPagerAdapter.ViewHolder(view) {


        private val ivPlayVideoButton: ImageView = view.findViewById(R.id.iv_play_video_button)
        private val photoView: PhotoView = view.findViewById<PhotoView>(R.id.pv_media_viewer).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        internal val isScaled: Boolean
            get() = photoView.scale > 1f

        internal val imageView: ImageView
            get() = photoView

        fun bind(position: Int) {
            this.position = position
            itemView.tag = position
            if (dataList[position].viewType == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                ivPlayVideoButton.visible()
                photoView.isZoomable = false
            } else {
                ivPlayVideoButton.gone()
            }
            ivPlayVideoButton.setOnClickListener {
                onVideoClicked(dataList[position])
            }
            Glide.with(itemView)
                    .load(dataList[position].getActualStringUri())
                    .placeholder(itemView.context.createProgressBar())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (position == startPosition) {
                                photoView.post {
                                    resourceReady?.invoke()
                                }
                            }
                            return false
                        }
                    })
                    .into(photoView)

        }

        fun resetScale() {
            photoView.setScale(photoView.minimumScale, true)
        }
    }
}
