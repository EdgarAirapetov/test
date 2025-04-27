package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.SurfaceView
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.click
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.db.models.PostViewLocalData
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.CacheUtil
import com.numplates.nomera3.modules.feed.ui.entity.FeatureData
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.VideoUtil
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewDetectViewHolder
import com.numplates.nomera3.modules.posts.ui.view.SwitchAudioView
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect
import com.numplates.nomera3.presentation.view.utils.zoomy.TapListener
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val DEFAULT_ASPECT_RATIO = 1.0

class MeeraFeatureHolder(
    var cacheUtil: CacheUtil?,
    view: View,
    val parentWidth: Int
) : MeeraBasePostHolder(
    view = view,
), VideoViewHolder, IPostViewDetectViewHolder {

    private val clContent = itemView.findViewById<ConstraintLayout>(R.id.cl_feature_content)
    private val tvTextPost = itemView.findViewById<MeeraTextViewWithImages>(R.id.tv_text_post)
    private val btnAction = itemView.findViewById<UiKitButton>(R.id.tv_btn_action)
    private val btnDismiss = itemView.findViewById<UiKitButton>(R.id.tv_btn_dismiss)
    private val ivCloseAnnounce: ImageView? = itemView.findViewById(R.id.iv_close)
    private val mediaContainer: FrameLayout = itemView.findViewById(R.id.media_container)
    private val videoDurationView: SwitchAudioView = itemView.findViewById(R.id.vdv_feature_video_duration)
    private val player: PlayerView = itemView.findViewById(R.id.video_post_view)
    private var aspect: Double = DEFAULT_ASPECT_RATIO
    private var videoUrl: String? = ""
    private var videoDurationInSeconds: Int = 0
    private var featureData: FeatureData? = null
    private var volumeState: VolumeState = VolumeState.OFF

    private var playerJob: Job? = null

    private var videoPlayer: ExoPlayer? = null
    private var videoPlayerListener : Player.Listener? = null

    private var contentMarginTop = 16.dp

    private var btnActionListener: OnClickListener? = null
    private var btnDismissListener: OnClickListener? = null
    private var btnCloseAnnounceListener: OnClickListener? = null
    private var zoomListener : ZoomListener? = null
    private var zoomTapListener : TapListener? = null

    override fun bind(post: PostUIEntity) {
        this.featureData = post.featureData
        setupContent(post)
        setVolume(volumeStateCallback?.getVolumeState() ?: VolumeState.OFF)
    }

    override fun setupContent(post: PostUIEntity) {
        post.featureData?.let {
            aspect = if(it.aspect != null && it.aspect > 0 ) {
                it.aspect
            } else {
                DEFAULT_ASPECT_RATIO
            }
            setupFeatureText(it)
            setupClickListeners(it)
            val mediaWidth = parentWidth - getViewParentHorizontalPaddings(ivPicture)
            val layoutParams = ConstraintLayout.LayoutParams(
                mediaWidth,
                (mediaWidth / aspect).toInt()
            )
            ivPicture?.layoutParams = layoutParams
            if (featureData?.image.isNullOrEmpty() && featureData?.video.isNullOrEmpty()) {
                ivPicture?.gone()
            } else {
                ivPicture?.visible()
                ivPicture?.loadGlide(featureData?.image)
            }

            val constraintSet = ConstraintSet()
            constraintSet.clone(clContent)
            constraintSet.connect(
                R.id.ivPicture,
                ConstraintSet.TOP,
                R.id.v_separator_top,
                ConstraintSet.BOTTOM,
            )
            constraintSet.setMargin(R.id.ivPicture, ConstraintSet.TOP, contentMarginTop)
            constraintSet.applyTo(clContent)

            player.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, MEDIA_CORNER_RADIUS.toFloat())
                }
            }
            player.setClipToOutline(true)

            setupZoom(post)
            setupVideoIfExists(it)
        }
    }

    override fun holderIsNotAttachedToWindow() = !itemView.isAttachedToWindow

    fun setVolume(volumeState: VolumeState){
        this.volumeState = volumeState
        videoDurationView.setSoundState(volumeState)
        videoDurationView.click {
            val newVolume = when (volumeStateCallback?.getVolumeState() ?: VolumeState.OFF) {
                VolumeState.ON -> VolumeState.OFF
                VolumeState.OFF -> VolumeState.ON
            }
            videoDurationView.setSoundState(newVolume)
            volumeStateCallback?.setVolumeState(newVolume)
        }
    }

    private fun setupFeatureText(featureData: FeatureData) {
        if (!featureData.text.isNullOrEmpty()) {
            featureData.tagSpan?.let { notNullSpan ->
                tvTextPost?.let { tvText ->
                    spanTagsText(
                        context = itemView.context,
                        tvText = tvText,
                        post = notNullSpan,
                        linkColor = R.color.uiKitColorForegroundLink
                    ) { clickType ->
                        postCallback?.onTagClicked(
                            clickType = clickType,
                            adapterPosition = bindingAdapterPosition,
                            tagOrigin = TagOrigin.POST_TEXT
                        )
                    }
                }
            } ?: kotlin.run {
                tvTextPost?.setTextNoSpans(featureData.text)
            }
            tvTextPost?.visible()
        } else {
            tvTextPost?.gone()
        }
    }

    override fun getAccountType(): AccountTypeEnum = AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN

    override fun setupZoom(post: PostUIEntity) {
        mediaContainer.post { mediaContainer.setOnHierarchyChangeListener(null) }
        zoomyProvider?.let { provider ->
            zoomBuilder?.endZoom()
            zoomBuilder = provider.provideBuilder()
                .target(ivPicture)
                .interpolator(OvershootInterpolator())
            zoomBuilder?.register()
        }
        /**
         * При старте видео, [VideoFeedHelper] добавляет [PlayerView] в [media_container]. Ловим
         * добавление PlayerView с помощью ViewGroup.OnHierarchyChangeListener. Zoomy ловит жест
         * увеличения на child (текущая вью с видео), и применяет увеличение на duplicatedPlayerView.
         * Через методы onViewStartedZooming/onViewEndedZooming переключаем видеопоток между основной
         * вью и той которая увеличивается.
         * */
        mediaContainer.post {
            mediaContainer.setOnHierarchyChangeListener(object :
                ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, mainPlayerView: View?) {
                    if (mainPlayerView != null && mainPlayerView is PlayerView) {
                        val duplicatedPlayerViewForZooming = PlayerView(mainPlayerView.context)
                        val player = mainPlayerView.player
                        zoomListener = object : ZoomListener {
                            override fun onViewStartedZooming(view: View?) {
                                mainPlayerView.player = null
                                duplicatedPlayerViewForZooming.player = player
                                mediaContainer.invisible()
                                ivPicture?.invisible()
                            }

                            override fun onViewEndedZooming(view: View?) {
                                mainPlayerView.player = player
                                duplicatedPlayerViewForZooming.player = null
                                ivPicture?.visible()
                                mediaContainer.visible()
                            }
                        }
                        zoomTapListener = TapListener {
                            // VideoFeedHelper и FeedRecyclerView автоматически устанавливают
                            // onClickListener для вкл/выкл звука на media_container, поэтому
                            // достаточно вызвать performClick()
                            mediaContainer.performClick()
                        }
                        val builder = zoomyProvider?.provideBuilder()
                            ?.target(mainPlayerView)
                            ?.setTargetDuplicate(duplicatedPlayerViewForZooming)
                            ?.interpolator(OvershootInterpolator())
                            ?.zoomListener(zoomListener)
                            ?.tapListener(zoomTapListener)

                        builder?.register()
                    }
                    if (mainPlayerView != null && mainPlayerView is CustomControlView) {
                        videoDurationView.post {
                            videoDurationView.gone()
                        }
                    }
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {
                    if (child != null && child is PlayerView) {
                        ivPicture?.post { ivPicture.visible() }
                    }
                    videoDurationView.post {
                        videoDurationView.visible()
                    }
                }
            })
        }
    }

    override fun setupBlur(post: PostUIEntity) = Unit

    override fun updateVolume(volumeState: VolumeState) {
        setVideoPlayerVolume(volumeState)
        updateVolumeIcons(volumeState)

        if (videoPlayer?.playWhenReady.isTrue()) {
            player.requestFocus()
        }
    }

    private fun setupClickListeners(featureData: FeatureData) {
        val haveActionButton = !featureData.button.isNullOrEmpty()
        if (haveActionButton) {
            btnAction.visible()
            btnAction.text = featureData.button
        } else {
            btnAction.gone()
        }
        if (featureData.hideable) {
            btnDismiss.visible()
        } else {
            btnDismiss.gone()
        }

        ivCloseAnnounce?.isVisible = featureData.isClosable

        btnDismiss?.text = featureData.dismissButton

        btnActionListener = OnClickListener {
            postCallback?.onFeatureClicked(
                featureId = featureData.id,
                haveAction = haveActionButton,
                dismiss = false,
                deepLink = featureData.deepLink,
                featureText = featureData.text,
            )
        }
        btnAction.setOnClickListener(btnActionListener)

        btnDismissListener = OnClickListener {
            postCallback?.onFeatureClicked(
                featureId = featureData.id,
                haveAction = haveActionButton,
                dismiss = true,
                featureText = featureData.text,
            )
        }
        btnDismiss.setOnClickListener(btnDismissListener)

        btnCloseAnnounceListener = OnClickListener {
            postCallback?.onFeatureClicked(
                featureId = featureData.id,
                haveAction = haveActionButton,
                dismiss = true,
                featureText = featureData.text,
            )
        }
        ivCloseAnnounce?.setOnClickListener(btnCloseAnnounceListener)
    }

    private fun setupVideoIfExists(featureData: FeatureData) {
        if (featureData.video.isNullOrEmpty()) {
            videoDurationView.gone()
            mediaContainer.gone()
            return
        } else {
            videoDurationView.visible()
            mediaContainer.visible()
            cacheUtil?.startCache(featureData.video)
        }
        ivPicture?.glideClear()
        videoUrl = featureData.video

        aspect = featureData.aspect ?: 1.0
        mediaContainer.layoutParams.height = 0
        itemView.tag = this

        videoDurationView.setSoundState(volumeState)
        videoDurationView.isVisible = player.player?.isPlaying != true

        if (featureData.videoPreview.isNullOrEmpty()) {
            ivPicture?.loadGlide(videoUrl)
        } else {
            ivPicture?.loadGlide(featureData.videoPreview)
        }

    }

    override fun getPicture(): ImageView? {
        return ivPicture
    }

    override fun getMediaContainer(): FrameLayout {
        return mediaContainer
    }

    override fun getMediaContainerForVolume(): FrameLayout {
        return mediaContainer
    }

    override fun getVideoDurationViewContainer() = videoDurationView

    override fun getVideoUrlString(): String? = featureData?.video

    override fun getItemView(): View {
        return itemView
    }

    override fun getVideoDuration(): Int {
        return videoDurationInSeconds
    }

    override fun needToPlay(): Boolean {
        return !featureData?.video.isNullOrEmpty()
    }

    override var onShowPostClicked: (() -> Unit)? = null

    override fun clearResources() {
        tvTextPost?.clearResources()
        btnAction?.setOnClickListener(null)
        btnDismiss?.setOnClickListener(null)
        ivCloseAnnounce?.setOnClickListener(null)
        btnActionListener = null
        btnDismissListener = null
        btnCloseAnnounceListener = null
        zoomListener = null
        zoomTapListener = null
        zoomBuilder?.clearResources()
        zoomBuilder?.endZoom()
        zoomBuilder = null
        cacheUtil = null
        detachPlayer()
        super.clearResources()
    }

    override fun getVideoPlayerView() = player

    override fun startPlayingVideo(position: Long?) {
        position?.let { videoPlayer?.seekTo(it) }
        fixSurfaceViewRefreshIssue()
        videoPlayer?.playWhenReady = true

        player.visible()
        player.alpha = 1f
        player.requestFocus()
    }

    override fun stopPlayingVideo() {
        videoPlayer?.playWhenReady = false
    }

    override fun initPlayer() {
        playerJob = CoroutineScope(Dispatchers.Main).launch {
            val media = featureData?.video ?: return@launch
            val context = itemView.context?.applicationContext ?: return@launch

            videoPlayer = VideoUtil.getFeedHolderPlayer(context)
                .also { it.repeatMode = Player.REPEAT_MODE_ALL }

            player.player = videoPlayer

            val mediaSource = VideoUtil.getMeeraFeedHolderPlayerMediaSource(context, media)

            videoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
            }

            volumeStateCallback?.getVolumeState()?.let { volumeState ->
                setVideoPlayerVolume(volumeState)
            }

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

        player.player?.release()
        player.player = null
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
        }
    }

    private fun fixSurfaceViewRefreshIssue() {
        (player.videoSurfaceView as? SurfaceView)?.apply {
            holder?.setFormat(PixelFormat.TRANSPARENT)
            holder?.setFormat(PixelFormat.OPAQUE)
            bringToFront()
        }
    }

    private fun updateVolumeIcons(volumeState: VolumeState) {
        videoDurationView.setSoundState(volumeState)
    }

    override fun getPostViewData(): PostViewLocalData {
        return PostViewLocalData(
            featureId = featureData?.id ?: -1,
            postUserId = postUIEntity?.getUserId() ?: -1,
            isFeaturePost = true
        )
    }

    override fun getViewAreaCollisionRect(): Rect {
        val mediaContainerRect = mediaContainer.globalVisibleRect
        val textRect = tvText?.globalVisibleRect
        val musicPlayer = musicPlayerCell?.globalVisibleRect
        return mediaContainerRect.merge(textRect).merge(musicPlayer)
    }

    private fun getViewParentHorizontalPaddings(view: View?): Int {
        val parent = view?.parent as? ConstraintLayout?
        val paddingStart = parent?.paddingStart ?: 0
        val paddingEnd = parent?.paddingStart ?: 0
        return paddingStart + paddingEnd
    }

}
