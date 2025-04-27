package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithPositioning
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle
import com.numplates.nomera3.modules.remotestyle.presentation.applyStyle
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.PostOnlyTextFormatter
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.utils.getTrueTextLengthWithProfanity
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy


class RepostViewHolder(
    postCallback: PostCallback,
    private val view: View,
    var contentManager: ISensitiveContentManager,
    var blurHelper: BlurHelper,
    var zoomyProvider: Zoomy.ZoomyProvider?,
    val parentWeight: Int,
    audioFeedHelper: AudioFeedHelper?,
    needToShowCommunityLabel: Boolean = true,
    isPostsWithBackgroundEnabled: Boolean = false,
    val isNeedMediaPositioning: Boolean = false
) : BaseRepostHolder(postCallback, view, contentManager, audioFeedHelper, blurHelper, needToShowCommunityLabel, isPostsWithBackgroundEnabled),
    PostOnlyTextFormatter.PostOnlyTextRemoteStyleView {

    private var accountType = AccountTypeEnum.ACCOUNT_TYPE_REGULAR
    private val sensitiveContentRepost: View? = view.findViewById(R.id.sensetive_content_repost)
    private val cvShowPost: FrameLayout? = view.findViewById(R.id.cv_show_post)

    override fun bind(post: PostUIEntity) {
        accountType = post.user?.accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR
        super.bind(post)
    }

    override fun setupContent(post: PostUIEntity) {
        val parentPost = post.parentPost
        val assets = parentPost?.assets
        val media = if (!assets.isNullOrEmpty()) assets[0] else null
        val imageUrl = getMediaUrl(post)
        val aspect = getMediaAspect(post)
        val mediaPositioning = if (!parentPost?.getImageUrl().isNullOrEmpty()) {
            parentPost?.getSingleMediaPositioning()
        } else {
            media?.mediaPositioning
        }

        if (!imageUrl.isNullOrEmpty()) {
            ivPicture?.invisible()
            aspect?.let { nonNullAspect: Double ->
                setupImageAspect(nonNullAspect, parentWeight - SUM_LEFT_RIGHT_MARGIN)
            }
            ivPicture?.loadGlideWithPositioning(
                path = imageUrl,
                positionY = mediaPositioning?.y,
                positionX = mediaPositioning?.x
            )
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
            zoomBuilder
                ?.tapListener {
                    postCallback?.onCommentClicked(post, bindingAdapterPosition)
                }
                ?.register()
        }
    }

    override fun setupBlur(post: PostUIEntity) {
        val imageUrl = getMediaUrl(post)
        val aspect = getMediaAspect(post)
        if ((post.parentPost?.isAdultContent == true
                && !imageUrl.isNullOrEmpty())
            && !contentManager.isMarkedAsNonSensitivePost(post.parentPost?.postId)
        ) {
            blurHelper.blurByUrl(imageUrl) {
                ivPicture?.invisible()
                if (aspect == ASPECT_16x9) {
                    ivStop32?.visible()
                    ivStop60?.gone()
                } else {
                    ivStop32?.gone()
                    ivStop60?.visible()
                }
                ivBluredContent?.loadGlide(it)
                sensitiveContentRepost?.visible()
                cvShowPost?.setOnClickListener {
                    contentManager.markPostAsNotSensitiveForUser(
                        post.parentPost.postId,
                        post.postId
                    )
                    sensitiveContentRepost?.gone()
                    ivPicture?.visible()
                }
            }
        } else {
            if (!imageUrl.isNullOrEmpty()) {
                ivPicture?.visible()
            } else {
                ivPicture?.gone()
            }
            sensitiveContentRepost?.gone()
        }
    }

    override fun updateVolume(volumeState: VolumeState) = Unit

    override fun clearResource() = Unit

    override fun bindStyle(style: PostOnlyTextRemoteStyle.Style) {
        tvTextParent?.applyStyle(view.context, isVip(), style)
    }

    override fun getTextLength(): Int {
        return tvTextParent.getTrueTextLengthWithProfanity(spanData)
    }

    override fun canApplyOnlyTextStyle(): Boolean =
        ivPicture.isVisible.not() && musicPlayerCell.isVisible.not()

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
