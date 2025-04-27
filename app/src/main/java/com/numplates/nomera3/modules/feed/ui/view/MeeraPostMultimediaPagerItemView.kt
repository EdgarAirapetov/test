package com.numplates.nomera3.modules.feed.ui.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.fromMillisToSec
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadBitmap
import com.meera.core.extensions.loadGlideWithOptionsAndCallback
import com.meera.core.extensions.loadGlideWithPositioning
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.getDurationSeconds
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemPostMultimediaItemBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.VideoZoomDelegate
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

private const val ANIMATION_DURATION = 200L
private const val FIRST_FRAME_TIME = 0L

@SuppressLint("ClickableViewAccessibility")
class MeeraPostMultimediaPagerItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), CanPerformZoom {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_item_post_multimedia_item, this, false)
        .apply(::addView)
        .let(MeeraItemPostMultimediaItemBinding::bind)

    private var media: MediaAssetEntity? = null
    private var mediaPreviewHeight: Int = 0
    private var mediaPreviewWidth: Int = 0
    private var zoomyProvider: Zoomy.ZoomyProvider? = null
    private var zoomBuilder: Zoomy.Builder? = null
    private var videoZoomDelegate: VideoZoomDelegate? = null
    private var post: PostUIEntity? = null
    private var postCallback: MeeraPostCallback? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    private var canPerformZoom: CanPerformZoom? = null
    private var canPerformZoomListener: CanPerformZoom? = null
    private var contentManager: ISensitiveContentManager? = null
    private var zoomListener: ZoomListener? = null
    private var onItemClicked: ((MediaAssetEntity, PostUIEntity?) -> Unit)? = null
    private var position: Int = -1

    private var videoPlayer: ExoPlayer? = null
    private var videoPlayerListener : Player.Listener? = null
    private var videoDurationControl: CustomControlView? = null
    private var duplicate: ImageView? = null

    private var playerJob: Job? = null

    private val MEDIA_CORNER_RADIUS = 12.dp

    fun bind(
        onItemClicked: ((MediaAssetEntity, PostUIEntity?) -> Unit)?,
        media: MediaAssetEntity,
        mediaStrictHeight: Int = 0,
        mediaStrictWidth: Int = 0,
        zoomyProvider: Zoomy.ZoomyProvider?,
        post: PostUIEntity?,
        postCallback: MeeraPostCallback?,
        volumeStateCallback: VolumeStateCallback?,
        canPerformZoom: CanPerformZoom?,
        contentManager: ISensitiveContentManager?,
        position: Int,
        zoomListener: ZoomListener?
    ) {
        endZoom()
        this.position = position
        this.onItemClicked = onItemClicked
        this.media = media
        this.mediaPreviewWidth = mediaStrictWidth
        this.mediaPreviewHeight = mediaStrictHeight
        this.zoomyProvider = zoomyProvider
        this.post = post
        this.postCallback = postCallback
        this.volumeStateCallback = volumeStateCallback
        this.canPerformZoom = canPerformZoom
        this.contentManager = contentManager
        this.zoomListener = zoomListener

        resetView()
        visible()

        setupViews(media)
    }

    fun unbind() {
        detachVideoPlayer()
        binding.flPostMultimediaItemMediaContainer.setOnClickListener(null)
        videoDurationControl?.setOnClickListener(null)
        zoomyProvider = null
        zoomBuilder?.clearResources()
        zoomBuilder = null
        videoZoomDelegate = null
        post = null
        postCallback = null
        volumeStateCallback = null
        canPerformZoom = null
        contentManager = null
        zoomListener = null
        onItemClicked = null
        canPerformZoomListener = null
    }

    fun updateVolumeState(volumeState: VolumeState) {
        setVideoPlayerVolume(volumeState)
        updateVolumeIcons(volumeState)

        if (videoPlayer?.playWhenReady.isTrue()) {
            binding.pvPostMultimediaItemVideoView.requestFocus()
        }
    }

    fun getMediaPreviewHeight() = mediaPreviewHeight
    fun getMediaPreviewWidth() = mediaPreviewWidth

    fun getDurationView(): VideoDurationView? {
        if (!isVideoItem()) return null
        return binding.vdvPostMultimediaVideoDuration
    }

    fun getVideoPlayer(): PlayerView? {
        if (!isVideoItem()) return null
        return binding.pvPostMultimediaItemVideoView
    }

    fun startPlayingVideo(position: Long?) {
        if (!isVideoItem()) return

        if (videoPlayer == null) {
            playerJob?.cancel()
            initVideoPlayer(onFinished = { startPlayingVideoActions(position) })
            return
        }

        startPlayingVideoActions(position)
    }

    fun stopPlayingVideo() {
        if (!isVideoItem()) return

        videoPlayer?.playWhenReady = false

        removeVideoControlView()
        val mediaAsset = media

        val needBlur = if (mediaAsset != null) isNeedShowBlur(mediaAsset) else false
        binding.vdvPostMultimediaVideoDuration.isVisible = !needBlur
    }

    fun showVideoDuration() {
        if (!isVideoItem()) return

        updateVideoDurationViewVisibility(isPlaying = videoPlayer?.isPlaying ?: false)
    }

    fun hideVideoDuration() {
        if (!isVideoItem()) return

        videoDurationControl?.iconContainer?.gone()
        binding.vdvPostMultimediaVideoDuration.gone()
    }

    fun resetView() {
        with(binding) {
            ivPostMultimediaItemImageView.setImageDrawable(null)
        }
    }

    fun endZoom() {
        zoomBuilder?.endZoom()
        videoZoomDelegate?.endZoom()
    }

    override fun canZoom() = canPerformZoom?.canZoom() ?: true

    override fun onAttachedToWindow() {
        initVideoPlayerWithJob()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        detachVideoPlayer()
        super.onDetachedFromWindow()
    }

    private fun startPlayingVideoActions(position: Long?) {
        position?.let { videoPlayer?.seekTo(it) }
        fixSurfaceViewRefreshIssue()
        videoPlayer?.playWhenReady = true

        binding.pvPostMultimediaItemVideoView.apply {
            visible()
            alpha = 1f
            requestFocus()
        }

        initVideoDurationControlView()
    }

    private fun setupViews(media: MediaAssetEntity) {
        setupHeight(mediaPreviewHeight)
        setupMediaImageView(media)
    }

    private fun setupMediaImageView(media: MediaAssetEntity) {
        with(binding) {
            ivPostMultimediaItemImageView.apply {
                background = null
                visible()
            }
        }

        setupVideoDurationView(media)

        when (media.type) {
            MEDIA_IMAGE ->
                setupImageMedia(media, mediaPreviewWidth, mediaPreviewHeight)

            MEDIA_VIDEO ->
                setupVideoMedia(media, mediaPreviewWidth, mediaPreviewHeight)
        }
    }

    private fun isVideoItem() = media?.type == MEDIA_VIDEO

    private fun setupHeight(height: Int, needUpdate: Boolean = false) {
        if (this.measuredHeight == height && !needUpdate) return

        val minHeight = binding.root.minHeight
        val finalHeight = if (height < minHeight) minHeight else height

        if (this.measuredHeight == 0) {
            updateLayoutParams {
                this.height = finalHeight
            }
            return
        }

        with(ValueAnimator.ofInt(this.measuredHeight, finalHeight)) {
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                updateLayoutParams {
                    this.height = value
                }
            }
            duration = ANIMATION_DURATION
            start()
        }
    }

    private fun setupImageMedia(media: MediaAssetEntity, width: Int, height: Int) {
        with(binding) {
            pvPostMultimediaItemVideoView.gone()
            ivPostMultimediaItemImageView.visible()

            flPostMultimediaItemMediaContainer.setOnClickListener(null)

            ivPostMultimediaItemImageView.apply {
                updateLayoutParams {
                    this.width = width
                    this.height = height
                }

                loadGlideWithPositioning(
                    path = media.image,
                    positionY = media.mediaPositioning?.y,
                    positionX = media.mediaPositioning?.x,
                    isNeedToFitHorizontal = true,
                    onFinished = { setupZoomForImage(media) }
                )
            }
        }
    }

    private fun setupVideoMedia(media: MediaAssetEntity, width: Int, height: Int) {
        with(binding) {
            pvPostMultimediaItemVideoView.visible()
            flPostMultimediaItemMediaContainer.setThrottledClickListener { onItemClicked?.invoke(media, post) }

            pvPostMultimediaItemVideoView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, MEDIA_CORNER_RADIUS.toFloat())
                }
            }
            pvPostMultimediaItemVideoView.setClipToOutline(true)

            ivPostMultimediaItemImageView.apply {
                val preview =
                    if (media.videoPreview.isNullOrEmpty()) media.video else media.videoPreview
                val hasPreview = !preview.isNullOrEmpty()
                if (hasPreview) {
                    updateLayoutParams {
                        this.width = width
                        this.height = height
                    }
                    val options = listOf(
                        RequestOptions().frame(FIRST_FRAME_TIME)
                    )
                    loadGlideWithOptionsAndCallback(
                        path = preview,
                        options = options,
                        onFinished = { setupZoomForVideo(media) }
                    )
                }
                ivPostMultimediaItemImageView.isVisible = hasPreview
            }
        }
    }

    private fun setupZoomForImage(media: MediaAssetEntity) {
        zoomyProvider?.let { provider ->
            media.image?.let { img ->
                binding.ivPostMultimediaItemImageView.post {
                    zoomBuilder?.endZoom()
                    canPerformZoomListener = object : CanPerformZoom {
                        override fun canZoom(): Boolean = canPerformZoom?.canZoom() ?: true
                    }
                    zoomBuilder = provider.provideBuilder()
                        .target(binding.ivPostMultimediaItemImageView)
                        .setShiftY(media.mediaPositioning?.y)
                        .setShiftX(media.mediaPositioning?.x)
                        .interpolator(OvershootInterpolator())
                        .tapListener { onItemClicked?.invoke(media, post) }
                        .zoomListener(zoomListener)
                        .canPerformZoom(canPerformZoomListener)
                        .enableLongPressForZoom(needLongTapZoom(media))

                    if (img.endsWith(MEDIA_EXT_GIF) || doesImageOverflowContainer(media.aspect)) {
                        duplicate = ImageView(context)
                        duplicate?.loadBitmap(media.image) { bitmap ->
                            bitmap?.let { duplicate?.setImageBitmap(it) }
                        }
                        zoomBuilder?.setTargetDuplicate(duplicate)?.aspectRatio(media.aspect.toDouble())
                    }
                    zoomBuilder?.register()
                }
            }
        }
    }

    private fun doesImageOverflowContainer(aspect: Float): Boolean {
        if (mediaPreviewHeight == 0 || mediaPreviewWidth == 0) return false

        val calculatedHeight = mediaPreviewWidth / aspect
        val calculatedWidth = mediaPreviewHeight * aspect

        val overflowsVertically = calculatedHeight > mediaPreviewHeight
        val overflowsHorizontally = calculatedWidth > mediaPreviewWidth

        return overflowsVertically || overflowsHorizontally
    }

    private fun setupZoomForVideo(media: MediaAssetEntity) {
        runCatching {
            post {
                videoZoomDelegate?.endZoom()
                zoomyProvider?.let {
                    videoZoomDelegate = VideoZoomDelegate(
                        media = media,
                        itemView = this,
                        zoomyProvider = zoomyProvider,
                        canZoom = this,
                        externalZoomListener = zoomListener
                    )
                }
            }
        }.onFailure { throwable ->
            Timber.e(throwable)
        }
    }

    private fun setupVideoDurationView(media: MediaAssetEntity) {
        val isVideoItem = media.type == MEDIA_VIDEO
        if (!isVideoItem) {
            binding.vdvPostMultimediaVideoDuration.gone()
            return
        }

        val videoDurationInSeconds = media.duration ?: 0
        val videoPlayer = getVideoPlayer()?.player
        val currentPositionInSeconds = (videoPlayer?.contentPosition ?: 0L).fromMillisToSec().toInt()
        val duration = getDurationSeconds(
            timeSec = if (currentPositionInSeconds < videoDurationInSeconds) {
                videoDurationInSeconds - currentPositionInSeconds
            } else {
                videoDurationInSeconds
            }
        )
        binding.vdvPostMultimediaVideoDuration.apply {
            setDurationText(duration)
            val isNotPlaying = videoPlayer?.isPlaying != true
            isVisible = isNotPlaying && !isNeedShowBlur(media)
        }

        val volumeState = volumeStateCallback?.getVolumeState() ?: VolumeState.OFF
        when (volumeState) {
            VolumeState.ON -> binding.vdvPostMultimediaVideoDuration.setSoundOn()
            VolumeState.OFF -> binding.vdvPostMultimediaVideoDuration.setSoundOff()
        }
    }

    private fun isNeedShowBlur(media: MediaAssetEntity): Boolean {
        val url = if (media.videoPreview.isNullOrEmpty()) media.video else media.videoPreview

        val isNotMarked = !(contentManager?.isMarkedAsNonSensitivePost(post?.postId) ?: false)
        return post?.isAdultContent.isTrue() && url?.isNotEmpty().isTrue() && isNotMarked
    }

    private fun needLongTapZoom(media: MediaAssetEntity): Boolean {
        return media.isMediaOverflowsBy25Percent(mediaPreviewWidth, mediaPreviewHeight)
    }

    private fun initVideoPlayerWithJob() {
        if (!isVideoItem()) return

        playerJob = CoroutineScope(Dispatchers.Main).launch { initVideoPlayer() }
    }

    private fun detachVideoPlayer() {
        playerJob?.cancel()

        videoPlayer?.apply {
            videoPlayerListener?.apply { removeListener(this) }
            stop()
            release()
        }
        videoPlayer = null
        videoPlayerListener = null

        binding.pvPostMultimediaItemVideoView.player?.release()
        binding.pvPostMultimediaItemVideoView.player = null
        removeVideoControlView()
    }

    private fun initVideoPlayer(onFinished: (() -> Unit)? = null) {
        val media = this@MeeraPostMultimediaPagerItemView.media?.video ?: return
        val ctx = context?.applicationContext ?: return

        videoPlayer = VideoUtil.getFeedHolderPlayer(ctx)
            .also { it.repeatMode = Player.REPEAT_MODE_ALL }

        binding.pvPostMultimediaItemVideoView.player = videoPlayer

        val mediaSource = VideoUtil.getMeeraFeedHolderPlayerMediaSource(ctx, media)

        videoPlayer?.apply {
            setMediaSource(mediaSource)
            prepare()
        }

        val volumeState = volumeStateCallback?.getVolumeState() ?: VolumeState.OFF
        setVideoPlayerVolume(volumeState)

        clearVideoPlayerListener()
        initPlayerListener()

        videoPlayerListener?.apply { videoPlayer?.addListener(this) }

        onFinished?.invoke()
    }

    private fun initVideoDurationControlView() {
        removeVideoControlView()

        val params = FrameLayout.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT
        )
            .also {
                it.gravity = Gravity.BOTTOM or Gravity.END
            }
        val videoDurationPaddingPx = context.resources.getDimensionPixelSize(R.dimen.video_duration_padding)

        videoDurationControl = CustomControlView(
            context,
            R.layout.feed_exo_player_control_view_extended
        ).also { view ->
            view.apply {
                setPadding(videoDurationPaddingPx)
                disableTimeRound()
                setOnClickListener(object : DoubleOrOneClickListener() {
                    override fun onClick() {
                        toggleVolume()
                    }
                })

                player = videoPlayer

                binding.flPostMultimediaItemMediaContainer.addView(this, params)
                requestFocus()

                val volumeState = volumeStateCallback?.getVolumeState() ?: VolumeState.OFF
                when (volumeState) {
                    VolumeState.ON -> setSoundOn()
                    VolumeState.OFF -> setSoundOff()
                }

                val isPlaying = videoPlayer?.isPlaying.isTrue()
                updateVideoDurationViewVisibility(isPlaying)
            }
        }
    }

    private fun clearVideoPlayerListener() {
        videoPlayerListener?.apply { videoPlayer?.removeListener(this) }.also { videoPlayerListener = null }
    }

    private fun removeVideoControlView() {
        videoDurationControl?.let {
            it.player = null
            binding.flPostMultimediaItemMediaContainer.removeView(it)
        }.also { videoDurationControl = null }
    }

    private fun toggleVolume() {
        if (videoPlayer == null) return

        val volumeState = volumeStateCallback?.getVolumeState() ?: VolumeState.OFF
        val newVolume = when (volumeState) {
            VolumeState.ON -> VolumeState.OFF
            VolumeState.OFF -> VolumeState.ON
        }

        setVideoPlayerVolume(newVolume)
        updateVolumeIcons(newVolume)
        volumeStateCallback?.setVolumeState(newVolume)
    }

    private fun setVideoPlayerVolume(volumeState: VolumeState) {
        videoPlayer?.volume = when (volumeState) {
            VolumeState.ON -> 1f
            VolumeState.OFF -> 0f
        }
    }

    private fun updateVolumeIcons(volumeState: VolumeState) {
        when (volumeState) {
            VolumeState.ON -> {
                videoDurationControl?.setSoundOn()
                binding.vdvPostMultimediaVideoDuration.setSoundOn()
            }
            VolumeState.OFF -> {
                videoDurationControl?.setSoundOff()
                binding.vdvPostMultimediaVideoDuration.setSoundOff()
            }
        }
    }

    private fun fixSurfaceViewRefreshIssue() {
        (binding.pvPostMultimediaItemVideoView.videoSurfaceView as? SurfaceView)?.apply {
            holder?.setFormat(PixelFormat.TRANSPARENT)
            holder?.setFormat(PixelFormat.OPAQUE)
            bringToFront()
        }
    }

    private fun initPlayerListener() {
        videoPlayerListener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                keepScreenOn = playWhenReady
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateVideoDurationView(isPlaying)
            }
        }
    }

    private fun updateVideoDurationView(isPlaying: Boolean) {
        val duration = videoDurationControl?.player?.duration ?: 0L
        val currentPositionInSeconds = videoDurationControl?.player?.contentPosition ?: 0L
        if (duration != 0L) {
            binding.vdvPostMultimediaVideoDuration
                .setDurationText(
                    getDurationSeconds(
                        (duration - currentPositionInSeconds).fromMillisToSec().toInt()
                    )
                )
        }

        updateVideoDurationViewVisibility(isPlaying)
    }

    private fun updateVideoDurationViewVisibility(isPlaying: Boolean) {
        post {
            binding.vdvPostMultimediaVideoDuration.isVisible = isVideoItem() && !isPlaying
            videoDurationControl?.iconContainer?.isVisible = isVideoItem() && isPlaying
        }
    }
}
