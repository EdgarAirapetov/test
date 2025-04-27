package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import android.view.View.OnClickListener
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithCallback
import com.meera.core.extensions.loadGlideWithPositioning
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.PostOnlyTextFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.utils.getTrueTextLengthWithProfanity
import com.numplates.nomera3.presentation.view.utils.zoomy.TapListener


class MeeraRepostViewHolder(
    val view: View,
    val parentWeight: Int,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackgroundEnabled: Boolean = false,
    val isNeedMediaPositioning: Boolean = false,
    val isSetRepostMode: Boolean = false
) : MeeraBaseRepostHolder(view, needToShowCommunityLabel, isPostsWithBackgroundEnabled),
    PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private var isSensitiveContentVisible = false
    private var zoomTapListener: TapListener? = null
    private var uiKitButtonShowPostClickListener: OnClickListener? = null

    override fun bind(post: PostUIEntity) {
        if (isSetRepostMode) isRepost = true
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
    }

    override fun updateViewsWithPresetWidth() {
        val post = postUIEntity ?: return

        setupContent(post)
    }

    override fun setupContent(post: PostUIEntity) {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null
        val imageUrl = getMediaUrl(post)
        val eventResId = eventLabelUiMapper.mapEventPlaceholder(parentPost?.event)
        val aspect = getMediaAspect(post)
        val mediaPositioning = if (!parentPost?.getImageUrl().isNullOrEmpty()) {
            parentPost?.getSingleMediaPositioning()
        } else {
            media?.mediaPositioning
        }
        if (!imageUrl.isNullOrEmpty()) {
            ivPicture?.invisible()
            aspect?.let { nonNullAspect: Double ->
                val parentFinalWidth = postCallback?.getParentWidth() ?: parentWeight
                setupImageAspect(nonNullAspect, parentFinalWidth - SUM_LEFT_RIGHT_MARGIN)
            }
            ivPicture?.loadGlideWithPositioning(
                path = imageUrl,
                positionY = mediaPositioning?.y,
                positionX = mediaPositioning?.x
            )
        } else if (eventResId != null) {
            ivPicture?.apply {
                aspect?.let { nonNullAspect: Double ->
                    val parentFinalWidth = postCallback?.getParentWidth() ?: parentWeight
                    setupImageAspect(nonNullAspect, parentFinalWidth - SUM_LEFT_RIGHT_MARGIN)
                }
                scaleType = ImageView.ScaleType.FIT_XY
                visible()
                loadGlideWithCallback(
                    path = eventResId,
                    onFinished = { setupZoom(post) }
                )
            }
        } else {
            ivPicture?.gone()
        }
        parentPost?.let {
            setupMusicCell(it)
            setupEvent(it)
        }
    }

    override fun getAccountType() = accountType

    override fun setupZoom(post: PostUIEntity) {
        val imageUrl = getMediaUrl(post)
        val aspect = getMediaAspect(post) ?: 1.0
        post.parentPost?.let { parentPost ->
            zoomBuilder?.endZoom()
            zoomBuilder = zoomyProvider?.provideBuilder()
                ?.target(ivPicture)
                ?.interpolator(OvershootInterpolator())
            if (imageUrl?.endsWith(".gif") == true || aspect < MIN_ASPECT) {
                val duplicate = ImageView(itemView.context)
                // load gif
                duplicate.loadGlide(imageUrl)
                zoomBuilder?.setTargetDuplicate(duplicate)?.aspectRatio(aspect)
            }
            zoomTapListener = TapListener { postCallback?.onCommentClicked(post, bindingAdapterPosition) }
            zoomBuilder?.tapListener(zoomTapListener)?.register()
        }
    }

    override fun setupBlur(post: PostUIEntity) {
        contentManager?.let { cm ->
            val imageUrl = getMediaUrl(post)
            val isMarked = !cm.isMarkedAsNonSensitivePost(post.parentPost?.postId)
            if (post.parentPost?.isAdultContent == true && imageUrl?.isEmpty() == false && isMarked) {
                blurHelper?.blurByUrl(imageUrl) {
                    isSensitiveContentVisible = true
                    ivPicture?.invisible()
                    ivBluredContent?.loadGlide(it)
                    sensitiveContent?.visible()
                    uiKitButtonShowPost?.buttonType = ButtonType.FILLED
                    uiKitButtonShowPostClickListener = OnClickListener {
                        isSensitiveContentVisible = false
                        cm.markPostAsNotSensitiveForUser(
                            post.parentPost?.postId,
                            post.postId
                        )
                        sensitiveContent?.gone()
                        ivPicture?.visible()
                    }
                    uiKitButtonShowPost?.setOnClickListener(uiKitButtonShowPostClickListener)
                }
            } else {
                isSensitiveContentVisible = false
                sensitiveContent?.gone()
                if (imageUrl.isNullOrEmpty().not() || post.parentPost?.isEvent().isTrue()) {
                    ivPicture?.visible()
                } else {
                    ivPicture?.gone()
                }
            }
        }
    }

    override fun updateVolume(volumeState: VolumeState) = Unit

    override fun clearResources() {
        zoomBuilder?.endZoom()
        zoomBuilder?.clearResources()
        zoomBuilder = null
        zoomTapListener = null
        uiKitButtonShowPostClickListener = null
        uiKitButtonShowPost?.setOnClickListener(null)
        super.clearResources()
    }

    override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
        tvTextParent?.applyStyle(view.context, isVip(), style)
    }

    override fun getTextLength(): Int {
        return tvTextParent.getTrueTextLengthWithProfanity(spanData)
    }

    override fun canApplyOnlyTextStyle(): Boolean =
        postUIEntity?.parentPost?.containsMedia().isNotTrue()

    override fun getLinesCount(): Int {
        return tvTextParent?.lineCount ?: 0
    }

    override fun getMediaUrl(post: PostUIEntity): String? {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null

        return if (!parentPost?.getImageUrl().isNullOrEmpty()) {
            parentPost?.getImageUrl()
        } else {
            media?.image
        }
    }

    override fun getMediaAspect(post: PostUIEntity): Double? {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null

        return if (!parentPost?.getImageUrl().isNullOrEmpty()) {
            parentPost?.getSingleAspect()
        } else {
            media?.aspect?.toDouble()
        }
    }
}
