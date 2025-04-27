package com.numplates.nomera3.modules.feed.ui.viewholder

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewDetectViewHolder
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.PostOnlyTextFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.utils.getTrueTextLengthWithProfanity
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener

private const val MEERA_FORCE_VIDEO_PLAY_DELAY = 500L

class MeeraMultimediaPostHolder(
    val view: View,
    val parentWidth: Int,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackground: Boolean = false,
    val isNeedMediaPositioning: Boolean = false
) : MeeraBasePostHolder(
    view,
    needToShowCommunityLabel,
    isPostsWithBackground
),
    VideoViewHolder, IPostViewDetectViewHolder, PostOnlyTextFormatter.PostOnlyTextRemoteStyleView,
    LongIndicatorViewHolder,
    CanPerformZoom,
    ZoomListener {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private var canZoom = true
    private var mediaExpandHandler = Handler(Looper.getMainLooper())
    private var isSensitiveContentVisible = false
    private val videoDurationView: VideoDurationView? = itemView.findViewById(R.id.vdv_post_video_duration)
    private val mediaContainer: FrameLayout? = itemView.findViewById(R.id.media_container)
    private var videoDurationInSeconds: Int = 0
    private var mediaContainerMaxWidth = 0
    private var mediaContainerMaxHeight = 0

    override fun bind(post: PostUIEntity) {
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
        hideDuration()
        handleEditProgress()
    }

    override fun updateViewsWithPresetWidth() {
        val post = postUIEntity ?: return

        setupMultimediaContent(post = post, forceBind = true)
    }

    fun getCurrentMediaPosition() = multimediaPager?.getCurrentMediaPosition() ?: -1

    override fun setupContent(post: PostUIEntity) {
        setupMultimediaContent(post)
    }

    override fun getAccountType() = accountType

    override fun setupClickListeners(post: PostUIEntity) {
        setupPostHeaderListener(post)
    }

    override fun setupZoom(post: PostUIEntity) = Unit

    override fun onViewStartedZooming(view: View?) {
        multimediaPager?.hideInterface()
        hideExpandMediaIndicatorView()
    }

    override fun onViewEndedZooming(view: View?) {
        multimediaPager?.showInterface()
        showExpandMediaIndicator()
    }

    override fun setupBlur(post: PostUIEntity) {
        val url = if (!post.getImageUrl().isNullOrEmpty()) {
            post.getImageUrl()
        } else {
            post.getSingleVideoPreview()
        }
        val isMarked = contentManager?.isMarkedAsNonSensitivePost(post.postId)!=true
        if (post.isAdultContent.isTrue() && !url.isNullOrEmpty() && isMarked) {
            multimediaPager?.disableSwipe()
            isSensitiveContentVisible = true
            ivPicture?.invisible()
            blurHelper?.blurByUrl(url) {
                ivBluredContent?.loadGlide(it)
                sensitiveContent?.visible()
                uiKitButtonShowPost?.buttonType = ButtonType.FILLED
                uiKitButtonShowPost?.setOnClickListener {
                    multimediaPager?.enableSwipe()
                    isSensitiveContentVisible = false
                    contentManager?.markPostAsNotSensitiveForUser(
                        post.postId,
                        post.parentPost?.postId
                    )
                    sensitiveContent?.gone()
                    ivPicture?.visible()
                    itemView.postDelayed(
                        { postCallback?.forceStartPlayingVideoRequested() },
                        MEERA_FORCE_VIDEO_PLAY_DELAY
                    )
                    startDelayedShow()
                }
                hideExpandMediaIndicatorView()
            }
        } else {
            multimediaPager?.enableSwipe()
            isSensitiveContentVisible = false
            sensitiveContent?.gone()
            if (post.assets.isNullOrEmpty().not()) ivPicture?.invisible()
            else ivPicture?.gone()
        }
    }

    override fun updateVolume(volumeState: VolumeState) {
        multimediaPager?.updateVolume(volumeState)
    }

    override fun clearResources() {
        stopPlayingVideo()
        onShowPostClicked = null
        mediaContainer?.setOnClickListener(null)
        zoomBuilder?.endZoom()
        zoomBuilder?.clearResources()
        zoomBuilder = null
        uiKitButtonShowPost?.setOnClickListener(null)
        multimediaPager?.unbind()
        super.clearResources()
    }

    override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
        tvText?.applyStyle(view.context, isVip(), style)
    }

    override fun getTextLength(): Int {
        return tvText.getTrueTextLengthWithProfanity(spanData)
    }

    override fun canApplyOnlyTextStyle(): Boolean = postUIEntity?.containsMedia().isNotTrue()

    override fun getLinesCount(): Int {
        return tvText?.lineCount ?: 0
    }

    override fun getRootItemView() = itemView

    override fun getLongMediaContainer() = multimediaPager

    override fun startDelayedShow() {
        val currentMedia = getCurrentMedia() ?: return
        when {
            isNotAbleToShowExpandMedia(currentMedia) -> hideExpandMediaIndicatorView()
            currentMedia.isMediaOverflowsBy25Percent(mediaContainerMaxWidth, mediaContainerMaxHeight) -> controlMediaExpandShow()
            else -> hideExpandMediaIndicatorView()
        }
    }

    override fun stopDelayedShow() {
        mediaExpandHandler.removeCallbacksAndMessages(null)
    }

    override fun hideLongIndicator() {
        hideExpandMediaIndicatorView()
    }

    override fun showExpandMediaIndicator() {
        postUIEntity?.let { post ->
            itemView.post {
                val currentMedia = getCurrentMedia() ?: return@post
                if (isNotAbleToShowExpandMedia(currentMedia) || !currentMedia.isMediaOverflowsBy25Percent(
                        mediaContainerMaxWidth, mediaContainerMaxHeight
                    )
                ) {
                    hideExpandMediaIndicatorView()
                } else {
                    showExpandMediaIndicatorView(false)
                }
            }
        }
    }

    private fun isNotAbleToShowExpandMedia(media: MediaAssetEntity?): Boolean {
        val isToggleEnabled = getFeatureToggles().feedMediaExpandFeatureToggle.isEnabled
        return !isToggleEnabled || media?.image.isNullOrEmpty() || sensitiveContent.isVisible
    }

    override fun getContentBarHeight(): Long {
        return contentBarHeight()
    }

    override var onShowPostClicked: (() -> Unit)? = null

    override fun getPicture(): ImageView? {
        return null
    }

    override fun getMediaContainer() = mediaContainer

    override fun getMediaContainerForVolume() = null

    override fun getVideoDurationViewContainer() = multimediaPager?.getCurrentDurationView()

    override fun getVideoUrlString() = multimediaPager?.getCurrentVideoUrl()

    override fun getItemView() = itemView

    override fun getVideoDuration() = videoDurationInSeconds

    override fun holderIsNotAttachedToWindow() = !itemView.isAttachedToWindow

    override fun needToPlay(): Boolean {
        val currentMedia = multimediaPager?.getCurrentMedia()
        val image = if (currentMedia?.videoPreview.isNullOrEmpty()) currentMedia?.video
        else currentMedia?.videoPreview
        return !((postUIEntity?.isAdultContent == true
            && !image.isNullOrEmpty()) && (contentManager?.isMarkedAsNonSensitivePost(postUIEntity?.postId)) != true)
            && !isEditProgress()
            && currentMedia?.type == MEDIA_VIDEO
    }

    override fun getVideoPlayerView() = multimediaPager?.getCurrentPlayer()

    override fun canZoom() = canZoom && isEditProgress().not() && !isSensitiveContentVisible

    override fun startPlayingVideo(position: Long?) {
        multimediaPager?.startPlayingVideo(position)
    }

    override fun stopPlayingVideo() {
        multimediaPager?.stopPlayingVideo()
    }

    override fun getSelectedMediaPosition(): Int? {
        return multimediaPager?.getCurrentMediaPosition()
    }

    override fun initPlayer() = Unit

    override fun detachPlayer() = Unit

    private fun setupMultimediaContent(post: PostUIEntity, forceBind: Boolean = false) {
        val assets = post.assets
        if (!assets.isNullOrEmpty()) {
            val parentFinalWidth = postCallback?.getParentWidth() ?: parentWidth
            mediaContainerMaxWidth = parentFinalWidth - (MEDIA_MARGIN_HORIZONTAL * 2)
            mediaContainerMaxHeight = setupImageAspect(assets[0].aspect.toDouble(), mediaContainerMaxWidth)
            multimediaPager?.bind(
                post = post,
                mediaPreviewMaxWidth = mediaContainerMaxWidth,
                mediaPreviewMaxHeight = mediaContainerMaxHeight,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                zoomyProvider = zoomyProvider,
                parentView = itemView,
                canPerformZoom = this,
                contentManager = contentManager,
                onMediaClicked = { media, currentPost ->
                    currentPost?.let {
                        if (isNeedShowBlur(currentPost)) return@let
                        postCallback?.onMediaClicked(it, media, bindingAdapterPosition)
                    }
                },
                zoomListener = this,
                forceBind = forceBind
            )

            ivPicture?.visible()
        } else {
            ivPicture?.gone()
            multimediaPager?.gone()
        }

        setupMusicCell(post)
        setupEvent(post)
    }

    private fun hideDuration() {
        videoDurationView?.gone()
    }

    private fun controlMediaExpandShow() {
        if (!isExpandMediaIndicatorViewVisible()) {
            mediaExpandHandler.removeCallbacksAndMessages(null)
            mediaExpandHandler.postDelayed({
                showExpandMediaIndicatorView(withAnimation = true)
            }, SHOW_EXPAND_MEDIA_INDICATOR_DELAY)
        } else {
            stopDelayedShow()
        }
    }

    private fun handleEditProgress() {
        if (isEditProgress()) {
            stopPlayingVideo()
            doubleClickContainer?.removeOnDoubleClickListener()
        }
    }

    companion object {
        const val SHOW_EXPAND_MEDIA_INDICATOR_DELAY = 300L
    }
}
