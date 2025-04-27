package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
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
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideoRepostHolder(
    var cacheUtilTool: CacheUtil,
    var contentManager: ISensitiveContentManager,
    var blurHelper: BlurHelper,
    var zoomyProvider: Zoomy.ZoomyProvider?,
    view: View,
    postCallback: PostCallback,
    val volumeStateCallback: VolumeStateCallback,
    val parentWeight: Int,
    audioFeedHelper: AudioFeedHelper?,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackgroundEnabled: Boolean = false
) : BaseRepostHolder(
    postCallback = postCallback,
    view = view,
    contentManager = contentManager,
    audioFeedHelper = audioFeedHelper,
    blurHelper = blurHelper,
    needToShowCommunityLabel = needToShowCommunityLabel,
    isPostsWithBackgroundEnabled = isPostsWithBackgroundEnabled
), VideoViewHolder {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private val flMediaContainer: FrameLayout? = itemView.findViewById(R.id.media_container)
    private val videoDurationView: VideoDurationView? = itemView.findViewById(R.id.vdv_parent_post_video_duration)
    private val videoView: PlayerView? = itemView.findViewById(R.id.video_post_view)
    private var post: PostUIEntity? = null

    private var playerJob: Job? = null

    private var videoPlayer: ExoPlayer? = null
    private var videoPlayerListener : Player.Listener? = null
    private var videoDurationControl: CustomControlView? = null

    override fun bind(post: PostUIEntity) {
        this.post = post
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
        setupVideoDurationLabel(post)
        getMediaUrl(post)?.let { cacheUtilTool.startCache(it) }
    }

    override fun setupContent(post: PostUIEntity) {
        val imgSrc = getPreview(post)
        val aspect = getMediaAspect(post) ?: 1.0
        if (post.isParentPostDeleted()) {
            ivPicture?.gone()
        } else if (!imgSrc.isNullOrEmpty()) {
            ivPicture?.invisible()
            setupImageAspect(aspect, parentWeight - SUM_LEFT_RIGHT_MARGIN)
            ivPicture?.loadGlide(imgSrc)
        }

        post.parentPost?.let {
            setupMusicCell(it)
        }
    }

    override fun setupClickListeners(post: PostUIEntity) {
        super.setupClickListeners(post)
        flMediaContainer?.setThrottledClickListener {
            postCallback?.onPostClicked(post, bindingAdapterPosition)
        }
    }

    private fun getPreview(post: PostUIEntity): String? {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null
        return when {
            !parentPost?.getSingleVideoPreview().isNullOrEmpty() -> parentPost?.getSingleVideoPreview()
            !parentPost?.getVideoUrl().isNullOrEmpty() -> parentPost?.getVideoUrl()
            !media?.videoPreview.isNullOrEmpty() -> media?.videoPreview
            else -> media?.video
        }
    }

    override fun holderIsNotAttachedToWindow() = !itemView.isAttachedToWindow

    override fun getAccountType() = accountType

    override fun setupZoom(post: PostUIEntity) {
        runCatching {
            itemView.post {
                val duplicatedPlayerViewForZooming = PlayerView(itemView.context)
                zoomBuilder?.endZoom()
                zoomBuilder = zoomyProvider?.provideBuilder()
                    ?.target(videoView)
                    ?.setTargetDuplicate(duplicatedPlayerViewForZooming)
                    ?.interpolator(OvershootInterpolator())
                    ?.zoomListener(object : ZoomListener {
                        override fun onViewStartedZooming(view: View?) {
                            val exoPlayer = videoView?.player
                            videoView?.player = null
                            duplicatedPlayerViewForZooming.player = exoPlayer
                            flMediaContainer?.invisible()
                            ivPicture?.invisible()
                        }

                        override fun onViewEndedZooming(view: View?) {
                            val exoPlayer = duplicatedPlayerViewForZooming.player ?: (view as? PlayerView?)?.player
                            duplicatedPlayerViewForZooming.player = null
                            videoView?.player = exoPlayer
                            ivPicture?.visible()
                            flMediaContainer?.visible()
                        }

                    })
                    ?.tapListener { flMediaContainer?.performClick() }
                    ?.canPerformZoom(object : CanPerformZoom {
                        override fun canZoom(): Boolean {
                            return videoView?.player?.isPlaying ?: false
                        }
                    })
                    ?.aspectRatio(getMediaAspect(post))
                zoomBuilder?.register()
            }
        }
    }

    override fun setupBlur(post: PostUIEntity) {
        val imgSrc = getPreview(post)
        if ((post.parentPost?.isAdultContent == true
                && !imgSrc.isNullOrEmpty())
            && !contentManager.isMarkedAsNonSensitivePost(post.parentPost.postId)
        ) {
            videoDurationView?.gone()
            ivMultimediaView?.gone()
            blurHelper.blurByUrl(imgSrc, imgSrc == getVideoUrlString()) {
                if (getMediaAspect(post) == ASPECT_16x9) {
                    ivStop32?.visible()
                    ivStop60?.gone()
                } else {
                    ivStop32?.gone()
                    ivStop60?.visible()
                }
                ivBluredContent?.loadGlide(it)
                sensitiveContent?.visible()
                flShowPost?.setOnClickListener {
                    contentManager.markPostAsNotSensitiveForUser(
                        post.parentPost.postId,
                        post.postId
                    )
                    showVideoDurationIfNeeded(post)
                    setupMultimediaIndicator(post)
                    onShowPostClicked?.invoke()
                    sensitiveContent?.gone()
                    ivPicture?.visible()
                }
            }
        } else {
            ivPicture?.visible()
            sensitiveContent?.gone()
        }
    }

    override fun updateVolume(volumeState: VolumeState) {
        setVideoPlayerVolume(volumeState)
        updateVolumeIcons(volumeState)

        if (videoPlayer?.playWhenReady.isTrue()) {
            videoView?.requestFocus()
        }
    }

    override fun clearResource() = detachPlayer()

    //VideoViewHolder methods
    override var onShowPostClicked: (() -> Unit)? = null

    override fun getPicture() = ivPicture

    override fun getMediaContainer() = flMediaContainer

    override fun getMediaContainerForVolume() = null

    override fun getVideoUrlString(): String? {
        return post?.let { getMediaUrl(it) }
    }

    override fun getItemView() = itemView

    override fun getVideoPlayerView() = videoView
    override fun startPlayingVideo(position: Long?) {
        if (post?.isParentPostDeleted().isTrue()) return

        position?.let { videoPlayer?.seekTo(it) }
        fixSurfaceViewRefreshIssue()
        videoPlayer?.playWhenReady = true

        videoView?.visible()
        videoView?.alpha = 1f
        videoView?.requestFocus()

        initVideoDurationControlView()
    }

    override fun stopPlayingVideo() {
        if (post?.isParentPostDeleted().isTrue()) return

        videoPlayer?.playWhenReady = false

        removeVideoControlView()
        post?.let { showVideoDurationIfNeeded(it) } ?: run { videoDurationView?.visible() }
    }

    override fun initPlayer() {
        playerJob = CoroutineScope(Dispatchers.Main).launch {
            val media = getVideoUrlString() ?: return@launch
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

    private fun setupVideoDurationLabel(post: PostUIEntity) {
        videoDurationView?.setDurationText(getDurationSeconds((getVideoDuration())))
        val isNotPlaying = videoView?.player?.isPlaying != true
        showVideoDurationIfNeeded(post = post, isNotPlaying = isNotPlaying)
    }

    private fun showVideoDurationIfNeeded(post: PostUIEntity, isNotPlaying: Boolean = true) {
        videoDurationView?.isVisible = isNotPlaying && !isNeedShowBlur(post.parentPost ?: post) && !post.isParentPostDeleted()
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
        val post = postUIEntity?.parentPost
        val needBlur = if (post != null) isNeedShowBlur(post) else false

        videoDurationView?.isVisible = !isPlaying && !needBlur
        videoDurationControl?.iconContainer?.isVisible = isPlaying && !needBlur
    }

    private fun removeVideoControlView() {
        videoDurationControl?.let { flMediaContainer?.removeView(it) }.also { videoDurationControl = null }
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

                flMediaContainer?.addView(this, params)
                requestFocus()

                val volumeState = volumeStateCallback.getVolumeState()
                when (volumeState) {
                    VolumeState.ON -> setSoundOn()
                    VolumeState.OFF -> setSoundOff()
                }

                iconContainer?.gone()
                post?.let { showVideoDurationIfNeeded(it) } ?: run { videoDurationView?.visible() }
            }
        }
    }

    private fun toggleVolume() {
        if (videoPlayer == null) return

        val volumeState = volumeStateCallback.getVolumeState()
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

    override fun getVideoDurationViewContainer() = videoDurationView

    override fun getVideoDuration(): Int {
        val parentPost = post?.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null

        val duration = if (!parentPost?.getVideoUrl().isNullOrEmpty()) {
            post?.parentPost?.getSingleVideoDuration()
        } else {
            media?.duration
        }

        return duration ?: 0
    }

    override fun needToPlay(): Boolean {
        val image = if (!post?.parentPost?.getSingleVideoPreview().isNullOrEmpty()) {
            post?.parentPost?.getSingleVideoPreview()
        } else {
            post?.parentPost?.getVideoUrl()
        }

        return !((post?.parentPost?.isAdultContent == true
            && !image.isNullOrEmpty()) && !contentManager.isMarkedAsNonSensitivePost(post?.parentPost?.postId))
    }

    override fun getMediaUrl(post: PostUIEntity): String? {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null

        return if (!parentPost?.getVideoUrl().isNullOrEmpty()) {
            parentPost?.getVideoUrl()
        } else {
            media?.video
        }
    }

    override fun getMediaAspect(post: PostUIEntity): Double? {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null

        return if (!parentPost?.getVideoUrl().isNullOrEmpty()) {
            parentPost?.getSingleAspect()
        } else {
            media?.aspect?.toDouble()
        }
    }

}
