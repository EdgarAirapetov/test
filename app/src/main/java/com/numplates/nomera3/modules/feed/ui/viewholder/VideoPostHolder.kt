package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.click
import com.meera.core.extensions.fromMillisToSec
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.getDurationSeconds
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.VideoZoomDelegate
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class VideoPostHolder(
    var cacheUtilTool: CacheUtil,
    var contentManager: ISensitiveContentManager,
    var blurHelper: BlurHelper,
    var zoomyProvider: Zoomy.ZoomyProvider?,
    postCallback: PostCallback,
    val volumeStateCallback: VolumeStateCallback,
    val view: View,
    val parentWidth: Int,
    audioFeedHelper: AudioFeedHelper?,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackgroundEnabled: Boolean = false
) : BasePostHolder(
    postCallback = postCallback,
    view = view,
    contentManager = contentManager,
    audioFeedHelper = audioFeedHelper,
    blurHelper = blurHelper,
    needToShowCommunityLabel = needToShowCommunityLabel,
    isPostsWithBackgroundEnabled = isPostsWithBackgroundEnabled
), VideoViewHolder, CanPerformZoom {

    private val videoDurationView: VideoDurationView? = itemView.findViewById(R.id.vdv_post_video_duration)
    private val mediaContainer: FrameLayout = itemView.findViewById(R.id.media_container)
    private val videoView: PlayerView? = itemView.findViewById(R.id.video_post_view)
    private var videoDurationInSeconds: Int = 0
    private var canZoom = true

    private var playerJob: Job? = null

    private var videoPlayer: ExoPlayer? = null
    private var videoPlayerListener : Player.Listener? = null
    private var videoDurationControl: CustomControlView? = null

    override fun bind(post: PostUIEntity) {
        super.bind(post)
        post.getVideoUrl()?.let { cacheUtilTool.startCache(it) }
        constraintCounter(post)
        handleEditProgress()
    }

    private fun handleEditProgress() {
        this.canZoom = isEditProgress().not()
        if (isEditProgress()) {
            videoView?.player?.stop()
            doubleClickContainer?.removeOnDoubleClickListener()
        }
    }

    private fun constraintCounter(post: PostUIEntity) {
        videoDurationInSeconds = post.getSingleVideoDuration() ?: 0
        val currentPositionInSeconds = (getVideoPlayerView()?.player?.contentPosition ?: 0L).fromMillisToSec().toInt()
        val duration = getDurationSeconds(
            timeSec = if (currentPositionInSeconds < videoDurationInSeconds) {
                videoDurationInSeconds - currentPositionInSeconds
            } else {
                videoDurationInSeconds
            }
        )
        videoDurationView?.setDurationText(duration)
        val isNotPlaying = videoView?.player?.isPlaying != true
        videoDurationView?.isVisible = isNotPlaying && !isNeedShowBlur(post)
    }

    override fun holderIsNotAttachedToWindow() = !itemView.isAttachedToWindow

    override fun setupContent(post: PostUIEntity) {
        itemView.tag = this
        val preview = if (post.getSingleVideoPreview().isNullOrEmpty()) {
            post.getVideoUrl()
        } else {
            post.getSingleVideoPreview()
        }
        if (preview.isNullOrEmpty().not()) {
            setupImageAspect(post.getSingleAspect(), parentWidth)
            ivPicture?.loadGlide(preview)
            ivPicture?.visible()
        } else {
            ivPicture?.gone()
        }
        setupMusicCell(post)
    }

    override fun setupClickListeners(post: PostUIEntity) {
        super.setupClickListeners(post)
        mediaContainer.setThrottledClickListener {
            postCallback?.onVideoClicked(post, bindingAdapterPosition)
        }
    }

    override fun getAccountType() = postUIEntity?.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR

    override fun setupZoom(post: PostUIEntity) {
        runCatching {
            itemView.post {
                videoZoomDelegate?.endZoom()
                videoZoomDelegate = VideoZoomDelegate(
                    post = post,
                    itemView = itemView,
                    zoomyProvider = zoomyProvider,
                    canZoom = this
                )
            }
        }.onFailure { throwable ->
            Timber.e(throwable)
        }
    }

    override fun setupBlur(post: PostUIEntity) {
        val image = if (post.getSingleVideoPreview().isNullOrEmpty()) {
            post.getVideoUrl()
        } else {
            post.getSingleVideoPreview()
        }

        if (isNeedShowBlur(post)) {
            ivPicture?.invisible()
            blurHelper.blurByUrl(image, image == post.getVideoUrl()) {
                if (post.getSingleAspect() == ASPECT_16x9) {
                    ivStop32?.visible()
                    ivStop60?.gone()
                } else {
                    ivStop32?.gone()
                    ivStop60?.visible()
                }
                ivBluredContent?.loadGlide(it)
                ivBluredContent?.click { }
                sensitiveContent?.visible()
                flShowPost?.setOnClickListener {
                    // TODO: 28.05.2021 возможно стоит вынести
                    contentManager.markPostAsNotSensitiveForUser(
                        post.postId,
                        post.parentPost?.postId
                    )
                    onShowPostClicked?.invoke()
                    sensitiveContent?.gone()
                    ivPicture?.visible()
                }
            }
        } else {
            sensitiveContent?.gone()
            ivPicture?.visible()
        }
    }

    override fun updateVolume(volumeState: VolumeState) {
        setVideoPlayerVolume(volumeState)
        updateVolumeIcons(volumeState)

        if (videoPlayer?.playWhenReady.isTrue()) {
            videoView?.requestFocus()
        }
    }

    override fun clearResource() {
        mediaContainer.setOnClickListener(null)
        detachPlayer()
    }

    override var onShowPostClicked: (() -> Unit)? = null

    override fun getPicture() = ivPicture

    override fun getMediaContainer() = mediaContainer
    override fun getMediaContainerForVolume() = null

    override fun getVideoUrlString() = postUIEntity?.getVideoUrl()

    override fun getItemView() = itemView

    override fun getVideoDurationViewContainer() = videoDurationView

    override fun getVideoDuration() = videoDurationInSeconds

    override fun needToPlay(): Boolean {
        val image = if (postUIEntity?.getSingleVideoPreview().isNullOrEmpty()) {
            postUIEntity?.getVideoUrl()
        } else {
            postUIEntity?.getSingleVideoPreview()
        }
        return !((postUIEntity?.isAdultContent == true
            && !image.isNullOrEmpty()) && !contentManager.isMarkedAsNonSensitivePost(postUIEntity?.postId))
            && !isEditProgress()
    }

    override fun getVideoPlayerView() = videoView

    override fun startPlayingVideo(position: Long?) {
        if (videoPlayer == null) return

        position?.let { videoPlayer?.seekTo(it) }
        fixSurfaceViewRefreshIssue()
        videoPlayer?.playWhenReady = true

        videoView?.apply {
            visible()
            alpha = 1f
            requestFocus()
        }

        initVideoDurationControlView()
    }

    override fun stopPlayingVideo() {
        videoPlayer?.playWhenReady = false
        removeVideoControlView()
        videoDurationView?.visible()
    }

    override fun initPlayer() {
        playerJob = CoroutineScope(Dispatchers.Main).launch {
            val media = postUIEntity?.getVideoUrl() ?: return@launch
            val context = itemView.context?.applicationContext ?: return@launch

            videoPlayer = VideoUtil.getFeedHolderPlayer(context).also { it.repeatMode = Player.REPEAT_MODE_ALL }

            videoView?.player = videoPlayer

            val mediaSource = VideoUtil.getFeedHolderPlayerMediaSource(context, media)

            videoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
            }

            val volumeState = volumeStateCallback.getVolumeState()
            setVideoPlayerVolume(volumeState)

            clearVideoPlayerListener()
            initPlayerListener()

            videoPlayerListener?.apply { videoPlayer?.addListener(this) }
        }
    }

    override fun detachPlayer() {
        playerJob?.cancel()

        videoPlayer?.apply {
            videoPlayerListener?.apply { removeListener(this) }
            stop()
            release()
        }
        videoPlayer = null
        videoPlayerListener = null

        videoView?.player?.release()
        videoView?.player = null
        removeVideoControlView()
    }

    private fun setVideoPlayerVolume(volumeState: VolumeState) {
        videoPlayer?.volume = when (volumeState) {
            VolumeState.ON -> 1f
            VolumeState.OFF -> 0f
        }
    }

    private fun clearVideoPlayerListener() {
        videoPlayerListener?.apply { videoPlayer?.removeListener(this) }.also { videoPlayerListener = null }
    }

    private fun initPlayerListener() {
        videoPlayerListener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                itemView.keepScreenOn = playWhenReady
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
            videoDurationView?.setDurationText(getDurationSeconds((duration - currentPositionInSeconds).fromMillisToSec().toInt()))
        }
        val post = postUIEntity
        val needBlur = if (post != null) isNeedShowBlur(post) else false

        videoDurationView?.isVisible = !isPlaying && !needBlur
        videoDurationControl?.iconContainer?.isVisible = isPlaying && !needBlur
    }

    private fun removeVideoControlView() {
        videoDurationControl?.let { mediaContainer.removeView(it) }.also { videoDurationControl = null }
    }

    private fun fixSurfaceViewRefreshIssue() {
        (videoView?.videoSurfaceView as? SurfaceView)?.apply {
            holder?.setFormat(PixelFormat.TRANSPARENT)
            holder?.setFormat(PixelFormat.OPAQUE)
            bringToFront()
        }
    }

    private fun initVideoDurationControlView() {
        removeVideoControlView()

        val params = FrameLayout.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            .also {
                it.gravity = Gravity.BOTTOM or Gravity.END
            }
        val videoDurationPaddingPx = itemView.context.resources.getDimensionPixelSize(R.dimen.video_duration_padding)

        videoDurationControl = CustomControlView(itemView.context, R.layout.feed_exo_player_control_view_extended).also { view ->
            view.apply {
                setPadding(videoDurationPaddingPx)
                disableTimeRound()
                setOnClickListener { toggleVolume() }

                player = videoPlayer

                mediaContainer.addView(this, params)
                requestFocus()

                val volumeState = volumeStateCallback.getVolumeState() ?: VolumeState.OFF
                when (volumeState) {
                    VolumeState.ON -> setSoundOn()
                    VolumeState.OFF -> setSoundOff()
                }

                iconContainer?.gone()
                videoDurationView?.visible()
            }
        }
    }

    private fun toggleVolume() {
        if (videoPlayer == null) return

        val volumeState = volumeStateCallback.getVolumeState() ?: VolumeState.OFF
        val newVolume = when (volumeState) {
            VolumeState.ON -> VolumeState.OFF
            VolumeState.OFF -> VolumeState.ON
        }

        setVideoPlayerVolume(newVolume)
        updateVolumeIcons(newVolume)
        volumeStateCallback.setVolumeState(newVolume)
    }

    private fun updateVolumeIcons(volumeState: VolumeState) {
        when (volumeState) {
            VolumeState.ON -> {
                videoDurationControl?.setSoundOn()
                videoDurationView?.setSoundOn()
            }
            VolumeState.OFF -> {
                videoDurationControl?.setSoundOff()
                videoDurationView?.setSoundOff()
            }
        }
    }

    override fun canZoom() = canZoom
}
