package com.numplates.nomera3.modules.feed.ui.viewholder

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadBitmap
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideCircle
import com.meera.core.extensions.loadGlideWithPositioning
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.MAX_ASPECT
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.PostOnlyTextFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.utils.getTrueTextLengthWithProfanity
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

class ImagePostHolder(
    var contentManager: ISensitiveContentManager,
    var blurHelper: BlurHelper,
    var zoomyProvider: Zoomy.ZoomyProvider?,
    postCallback: PostCallback?,
    val view: View,
    val parentWidth: Int,
    audioFeedHelper: AudioFeedHelper?,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackground: Boolean = false,
    val isNeedMediaPositioning: Boolean = false
) : BasePostHolder(postCallback, view, contentManager, audioFeedHelper, blurHelper, needToShowCommunityLabel, isPostsWithBackground),
    PostOnlyTextFormatter.PostOnlyTextRemoteStyleView, LongIndicatorViewHolder {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private var onImageClick: (() -> Unit)? = null
    private var isEditProgress = false
    private var isSensitiveContentVisible = false
    private var mediaExpandHandler = Handler(Looper.getMainLooper())

    /**
     * Settings ivPicture tap listener here to prevent DoubleOrOneClickListener logic errors during changes of
     * DoubleOrOneClickListener instances in case of payload updates when first click can be registered in one instance
     * and second in the next instance
     */
    override fun bind(post: PostUIEntity) {
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
        handleEditProgress()
        handlePictureClick()
    }

    private fun handleEditProgress() {
        this.isEditProgress = isEditProgress()
        if (isEditProgress()) {
            doubleClickContainer?.removeOnDoubleClickListener()
        }
    }

    private fun handlePictureClick() {
        ivPicture?.setOnClickListener(object : DoubleOrOneClickListener() {
            override fun onClick() {
                if (isEditProgress()) return
                onImageClick?.invoke()
            }
        })
    }

    override fun setupContent(post: PostUIEntity) {
        val url = post.getImageUrl()
        if (url.isNullOrEmpty().not()) {
            setupImageAspect(post.getSingleAspect(), parentWidth)
            val isNeedCircleImage = post.type == PostTypeEnum.AVATAR_VISIBLE || post.type == PostTypeEnum.AVATAR_HIDDEN
            ivPicture?.apply {
                val padding = if (isNeedCircleImage) dpToPx(24) else 0
                setPadding(padding, padding, padding, padding)
                scaleType = if (isNeedCircleImage) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.MATRIX
                if (isNeedCircleImage) {
                    loadGlideCircle(url)
                } else {
                    loadGlideWithPositioning(
                        path = url,
                        positionY = post.getSingleMediaPositioning()?.y,
                        positionX = post.getSingleMediaPositioning()?.x,
                        isNeedToFitHorizontal = true,
                        onFinished = { setupZoom(post) }
                    )
                }
                visible()
            }
        } else {
            clearImageView()
            setImageInitHeight()
            ivPicture?.gone()
            setupZoom(post)
        }
        setupMusicCell(post)
        setupEvent(post)
    }

    override fun getAccountType() = accountType

    override fun setupClickListeners(post: PostUIEntity) {
        setupPostHeaderListener(post)
        setupEventParticipantsListener(post)
//        setupEventAddressListener(post)
    }

    override fun setupZoom(post: PostUIEntity) {
        if (post.isNotExpandedSnippetState) {
            onImageClick = { postCallback?.onPostSnippetExpandedStateRequested(post) }
            return
        }
        val image = post.getImageUrl()
        val aspect = post.getSingleAspect()

        onImageClick = null
        zoomyProvider?.let { provider ->
            image?.let { img ->
                ivPicture?.post {
                    zoomBuilder?.endZoom()
                    zoomBuilder = provider.provideBuilder()
                        .target(ivPicture)
                        .setShiftY(post.getSingleMediaPositioning()?.y)
                        .setShiftX(post.getSingleMediaPositioning()?.x)
                        .interpolator(OvershootInterpolator())
                        .tapListener {
                            if (isEditProgress()) return@tapListener
                            clickPicture(post)
                        }
                        .canPerformZoom(object : CanPerformZoom{
                            override fun canZoom(): Boolean = isAbleToZoomImage()
                        })
                        .enableLongPressForZoom(needLongTapZoom(post))

                    if (img.endsWith(".gif") || aspect < MIN_ASPECT || aspect > MAX_ASPECT) {
                        val duplicate = ImageView(itemView.context)
                        // load gif
                        duplicate.loadBitmap(image) { bitmap ->
                            bitmap?.let { duplicate.setImageBitmap(it) }
                        }
                        zoomBuilder?.setTargetDuplicate(duplicate)?.aspectRatio(aspect)
                    }
                    zoomBuilder?.register()
                }
            }
        }
    }

    private fun clickPicture(post: PostUIEntity) {
        if (post.hasAssets()) {
            val mediaAsset = post.getSingleAsset() ?: return
            postCallback?.onMediaClicked(
                post = post,
                mediaAsset = mediaAsset,
                adapterPosition = absoluteAdapterPosition
            )
        } else {
            postCallback?.onPictureClicked(post)
        }
    }

    override fun setupBlur(post: PostUIEntity) {
        val isMarked = !contentManager.isMarkedAsNonSensitivePost(post.postId)
        val image = post.getImageUrl()
        if (post.isAdultContent == true && image?.isEmpty() == false && isMarked) {
            isSensitiveContentVisible = true
            ivPicture?.invisible()
            blurHelper.blurByUrl(image) {
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
                    isSensitiveContentVisible = false
                    contentManager.markPostAsNotSensitiveForUser(
                        post.postId,
                        post.parentPost?.postId
                    )
                    sensitiveContent?.gone()
                    ivPicture?.visible()
                    startDelayedShow()
                }
                hideExpandMediaIndicatorView()
            }
        } else {
            isSensitiveContentVisible = false
            sensitiveContent?.gone()
            if (image.isNullOrEmpty().not()) ivPicture?.visible()
            else ivPicture?.gone()
        }
    }

    override fun updateVolume(volumeState: VolumeState) = Unit

    override fun clearResource() = Unit

    override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
        tvText?.applyStyle(view.context, isVip(), style)
    }

    override fun getTextLength(): Int {
        return tvText.getTrueTextLengthWithProfanity(spanData)
    }

    override fun canApplyOnlyTextStyle(): Boolean = ivPicture.isVisible.not() && musicPlayerCell.isVisible.not()

    override fun getLinesCount(): Int {
        return tvText?.lineCount ?: 0
    }

    private fun needLongTapZoom(post: PostUIEntity): Boolean {
        return getFeatureToggles().feedMediaExpandFeatureToggle.isEnabled
            && post.isNeedToShowExpandView()
    }

    override fun getRootItemView() = itemView

    override fun getLongMediaContainer(): View? = ivPicture

    override fun startDelayedShow() {
        val post = postUIEntity?: return
        when {
            isNotAbleToShowExpandMedia(post) -> hideExpandMediaIndicatorView()
            post.isNeedToShowExpandView() -> controlMediaExpandShow()
            else -> hideExpandMediaIndicatorView()
        }
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

    override fun stopDelayedShow() {
        mediaExpandHandler.removeCallbacksAndMessages(null)
    }

    override fun hideLongIndicator() {
        hideExpandMediaIndicatorView()
    }

    override fun showExpandMediaIndicator() {
        postUIEntity?.let { post ->
            itemView.post {
                if (isNotAbleToShowExpandMedia(post)) {
                    hideExpandMediaIndicatorView()
                } else if (post.isNeedToShowExpandView()) {
                    showExpandMediaIndicatorView(false)
                }
            }
        }
    }

    private fun isNotAbleToShowExpandMedia(post: PostUIEntity) : Boolean {
        val isToggleEnabled = getFeatureToggles().feedMediaExpandFeatureToggle.isEnabled
        return !isToggleEnabled || !post.hasImage() || sensitiveContent?.isVisible.isTrue()
    }

    private fun isAbleToZoomImage(): Boolean {
        return !isEditProgress && !isSensitiveContentVisible
    }

    private fun setImageInitHeight() {
        val layoutParams = ivPicture?.layoutParams
        layoutParams?.height = 0
        ivPicture?.layoutParams = layoutParams
    }

    override fun getContentBarHeight(): Long {
        return contentBarHeight()
    }

    companion object {
        const val SHOW_EXPAND_MEDIA_INDICATOR_DELAY = 300L
    }

}
