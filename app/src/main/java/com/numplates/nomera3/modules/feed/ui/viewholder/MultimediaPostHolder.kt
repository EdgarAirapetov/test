package com.numplates.nomera3.modules.feed.ui.viewholder

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isGone
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewDetectViewHolder
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.PostOnlyTextFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.utils.getTrueTextLengthWithProfanity
import com.numplates.nomera3.presentation.view.ui.VideoViewHolder
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

private const val FORCE_VIDEO_PLAY_DELAY = 500L

class MultimediaPostHolder(
    var contentManager: ISensitiveContentManager,
    var blurHelper: BlurHelper,
    var zoomyProvider: Zoomy.ZoomyProvider?,
    postCallback: PostCallback,
    val volumeStateCallback: VolumeStateCallback,
    val view: View,
    val parentWidth: Int,
    audioFeedHelper: AudioFeedHelper?,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackground: Boolean = false,
    val isNeedMediaPositioning: Boolean = false
) : BasePostHolder(postCallback, view, contentManager, audioFeedHelper, blurHelper, needToShowCommunityLabel, isPostsWithBackground),
    VideoViewHolder, IPostViewDetectViewHolder, PostOnlyTextFormatter.PostOnlyTextRemoteStyleView, LongIndicatorViewHolder,
    CanPerformZoom {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private var canZoom = true
    private var mediaExpandHandler = Handler(Looper.getMainLooper())
    private var isSensitiveContentVisible = false
    private val videoDurationView: VideoDurationView? = itemView.findViewById(R.id.vdv_post_video_duration)
    private val mediaContainer: FrameLayout? = itemView.findViewById(R.id.media_container)
    private var videoDurationInSeconds: Int = 0

    override fun bind(post: PostUIEntity) {
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
        hideDuration()
        handleEditProgress()
    }

    private fun handleEditProgress() {
        if (isEditProgress()) {
            doubleClickContainer?.removeOnDoubleClickListener()
        }
    }

    fun getCurrentMediaPosition() = multimediaPager?.getCurrentMediaPosition() ?: -1

    override fun setupContent(post: PostUIEntity) {
        val assets = post.assets
        if (!assets.isNullOrEmpty()) {
            val asset = when (assets[0].type) {
                MEDIA_IMAGE -> assets[0].image
                else -> assets[0].videoPreview
            }
            val height = setupImageAspect(assets[0].aspect.toDouble(), parentWidth)
            multimediaPager?.bind(
                post = post,
                mediaPreviewMaxHeight = height,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                zoomyProvider = zoomyProvider,
                canZoom = canZoom,
                parentView = itemView,
                contentManager = contentManager,
                onMediaClicked = { media, currentPost ->
                    currentPost?.let { postCallback?.onMediaClicked(it, media, bindingAdapterPosition)  }
                },
                onScrollingPagerListener = { scrollInProcess ->
                    multimediaScrollingInProcess = scrollInProcess
                }
            )

            ivPicture?.loadGlide(asset)
            ivPicture?.visible()
        } else {
            ivPicture?.gone()
            multimediaPager?.gone()
        }

        setupMusicCell(post)
        setupEvent(post)
    }

    override fun getAccountType() = accountType

    override fun holderIsNotAttachedToWindow() = !itemView.isAttachedToWindow

    override fun setupClickListeners(post: PostUIEntity) {
        setupPostHeaderListener(post)
        setupEventParticipantsListener(post)
    }

    override fun setupZoom(post: PostUIEntity) = Unit

    override fun setupBlur(post: PostUIEntity) {
        val url = if (!post.getImageUrl().isNullOrEmpty()) {
            post.getImageUrl()
        } else {
            post.getSingleVideoPreview()
        }
        val isMarked = !contentManager.isMarkedAsNonSensitivePost(post.postId)
        if (post.isAdultContent.isTrue() && !url.isNullOrEmpty() && isMarked) {
            multimediaPager?.disableSwipe()
            isSensitiveContentVisible = true
            ivPicture?.invisible()
            blurHelper.blurByUrl(url) {
                if (post.getSingleAspect() == ASPECT_16x9) {
                    ivStop32?.visible()
                    ivStop60?.gone()
                } else {
                    ivStop32?.gone()
                    ivStop60?.visible()
                }
                ivBluredContent?.loadGlide(it)
                sensitiveContent?.visible()
                flShowPost?.setOnClickListener {
                    multimediaPager?.enableSwipe()
                    isSensitiveContentVisible = false
                    contentManager.markPostAsNotSensitiveForUser(
                        post.postId,
                        post.parentPost?.postId
                    )
                    sensitiveContent?.gone()
                    ivPicture?.visible()
                    itemView.postDelayed({ postCallback?.forceStartPlayingVideoRequested() }, FORCE_VIDEO_PLAY_DELAY)
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

    override fun clearResource() {
        mediaContainer?.setOnClickListener(null)
        multimediaPager?.clearResources()
    }

    override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
        tvText?.applyStyle(view.context, isVip(), style)
    }

    override fun getTextLength(): Int {
        return tvText.getTrueTextLengthWithProfanity(spanData)
    }

    override fun canApplyOnlyTextStyle(): Boolean =
        ivPicture.isVisible.not() && musicPlayerCell.isVisible.not() && multimediaPager?.isGone.isTrue()

    override fun getLinesCount(): Int {
        return tvText?.lineCount ?: 0
    }

    override fun getRootItemView() = itemView

    override fun getLongMediaContainer() = multimediaPager

    override fun startDelayedShow() {
        val currentMedia = getCurrentMedia() ?: return
        when {
            isNotAbleToShowExpandMedia(currentMedia) -> hideExpandMediaIndicatorView()
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
                if (isNotAbleToShowExpandMedia(currentMedia) || !doesImageOverflowBy25Percent(currentMedia)) {
                    hideExpandMediaIndicatorView()
                } else {
                    showExpandMediaIndicatorView(false)
                }
            }
        }
    }

    private fun doesImageOverflowBy25Percent(currentMedia: MediaAssetEntity): Boolean {
        val width = multimediaPager?.measuredWidth ?: return false
        val height = multimediaPager.measuredHeight

        if (width == 0 || height == 0) return false

        val aspect = currentMedia.aspect
        val calculatedHeight = width / aspect
        val calculatedWidth = height * aspect

        if (calculatedHeight > height) {
            val overflowHeight = calculatedHeight - height
            if (overflowHeight >= calculatedHeight * EXPANDED_MEDIA_THRESHOLD) {
                return true
            }
        }

        if (calculatedWidth > width) {
            val overflowWidth = calculatedWidth - width
            if (overflowWidth >= calculatedWidth * EXPANDED_MEDIA_THRESHOLD) {
                return true
            }
        }

        return false
    }

    private fun isNotAbleToShowExpandMedia(media: MediaAssetEntity?) : Boolean {
        val isToggleEnabled = getFeatureToggles().feedMediaExpandFeatureToggle.isEnabled
        return !isToggleEnabled || media?.image.isNullOrEmpty() || sensitiveContent.isVisible
    }

    override fun getContentBarHeight(): Long {
        return contentBarHeight()
    }

    companion object {
        const val SHOW_EXPAND_MEDIA_INDICATOR_DELAY = 300L
        const val EXPANDED_MEDIA_THRESHOLD = 0.25
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

    override fun needToPlay(): Boolean {
        val currentMedia = multimediaPager?.getCurrentMedia()
        val image = if (currentMedia?.videoPreview.isNullOrEmpty()) currentMedia?.video
        else currentMedia?.videoPreview
        return !((postUIEntity?.isAdultContent == true
            && !image.isNullOrEmpty()) && !contentManager.isMarkedAsNonSensitivePost(postUIEntity?.postId))
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

    override fun initPlayer() = Unit

    override fun detachPlayer() = Unit

    private fun hideDuration() {
        videoDurationView?.gone()
    }
}
