package com.numplates.nomera3.modules.feed.ui.viewholder

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.chat.ui.adapter.ChatMessagesAdapter
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelEvent
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelUiModel
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.posts.ui.view.CommunityLabelView
import com.numplates.nomera3.modules.posts.ui.view.PostHeaderView
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import timber.log.Timber

abstract class BaseRepostHolder(
    postCallback: PostCallback,
    view: View,
    contentManager: ISensitiveContentManager,
    audioFeedHelper: AudioFeedHelper?,
    blurHelper: BlurHelper,
    needToShowCommunityLabel: Boolean = true,
    private val isPostsWithBackgroundEnabled: Boolean = false
) : BasePostHolder(
    postCallback,
    view,
    contentManager,
    audioFeedHelper,
    blurHelper,
    needToShowCommunityLabel,
    isPostsWithBackgroundEnabled
) {

    protected val tvTitleParent: TextViewWithImages? = view.findViewById(R.id.tv_item_repost_title)
    protected val tvTextParent: TextViewWithImages? = view.findViewById(R.id.tvText_parent)
    protected val ivMultimediaView: View? = itemView.findViewById(R.id.iv_repost_multimedia_view)
    private val tvShowMoreText: TextView? = view.findViewById(R.id.tv_show_more_text)
    private val tvDeletedParentPostLabel: TextView? = view.findViewById(R.id.tv_deleted_parent_post_label)
    private val vSeparatorBottom: View? = view.findViewById(R.id.v_image_post_separator)
    override val sensitiveContent: FrameLayout? = itemView.findViewById(R.id.sensetive_content_repost)
    private val communityLabelView: CommunityLabelView? = itemView.findViewById(R.id.cl_parent_post_header)
    private val postHeaderView: PostHeaderView? = itemView.findViewById(R.id.phv_parent_post_header)
    private val parentContentLayout: ViewGroup? = itemView.findViewById(R.id.vg_parent_content)

    protected val SUM_LEFT_RIGHT_MARGIN = 20.dp

    override fun bind(post: PostUIEntity) {
        super.bind(post)
        setupParentPostBackground()
        setupParentPostHeader(post)
        setupParentPostTitle(post)
        setupParentPostText(post)
        setupParentTextBackground(post)
        hideParentPostIfDeleted(post)
        vSeparatorBottom?.visible()
        setupMultimediaIndicator(post)
    }

    override fun setupClickListeners(post: PostUIEntity) {
        ivPicture?.click { postCallback?.onCommentClicked(post, bindingAdapterPosition) }
        setupPostHeaderListener(post)
        eventParticipantsView?.setActionListener {
            postCallback?.onCommentClicked(post, bindingAdapterPosition)
        }
    }

    override fun resetView() {
        super.resetView()
        tvShowMoreText?.gone()
    }

    abstract fun getMediaUrl(post: PostUIEntity): String?

    abstract fun getMediaAspect(post: PostUIEntity): Double?

    protected fun setupMultimediaIndicator(post: PostUIEntity) {
        val needBlur = if (post.parentPost != null) isNeedShowBlur(post.parentPost) else false
        ivMultimediaView?.isVisible = !post.parentPost?.assets.isNullOrEmpty() && !post.isParentPostDeleted() && !needBlur
    }

    private fun setupParentPostBackground() {
        val parentBgResId = if (isVip()) R.drawable.frame_repost_container_vip else R.drawable.frame_repost_container
        parentContentLayout?.setBackgroundResource(parentBgResId)
    }

    private fun setupParentPostHeader(post: PostUIEntity) {
        val parentPost = post.parentPost ?: return
        if (parentPost.isCommunityPost()) {
            val communityLabelUiModel = CommunityLabelUiModel(
                post = parentPost,
                isVipTheme = false
            )
            communityLabelView?.bind(communityLabelUiModel)
            communityLabelView?.setEventListener { event ->
                when (event) {
                    is CommunityLabelEvent.CommunityClicked -> postCallback?.onCommunityClicked(
                        communityId = event.communityId,
                        adapterPosition = bindingAdapterPosition
                    )
                }
            }
            communityLabelView?.visible()
        } else {
            communityLabelView?.setEventListener(null)
            communityLabelView?.gone()
        }
        val postHeaderUiModel = PostHeaderUiModel(
            post = parentPost,
            navigationMode = PostHeaderNavigationMode.NONE,
            isOptionsAvailable = false,
            childPost = post,
            isCommunityHeaderEnabled = false,
            isLightNavigation = true
        )
        postHeaderView?.bind(postHeaderUiModel)
        postHeaderView?.setEventListener {
            if (!post.isParentPostDeleted()) {
                postCallback?.onPressRepostHeader(post, bindingAdapterPosition)
            }
        }
    }

    private fun setupParentPostTitle(post: PostUIEntity) {
        tvTitleParent ?: return
        if (isEventsEnabled.not()) {
            tvTitleParent.gone()
            return
        }
        val event = post.parentPost?.event
        if (event != null) {
            val textColor = ContextCompat.getColor(
                itemView.context,
                if (isVip()) R.color.white_85 else R.color.ui_black
            )
            tvTitleParent.setTextColor(textColor)
            val textColorLink = ContextCompat.getColor(
                itemView.context,
                if (isVip()) R.color.ui_yellow else R.color.ui_post_text_link
            )
            tvTitleParent.setLinkTextColor(textColorLink)
            if (post.parentPost.event.tagSpan != null) {
                val linkColor = if (isVip()) R.color.colorGoldCB8D else R.color.ui_purple
                spanTagsText(
                    context = itemView.context,
                    tvText = tvTitleParent,
                    post = post.parentPost.event.tagSpan,
                    linkColor = linkColor
                )
            } else {
                tvTitleParent.setTextNoSpans(post.parentPost.event.title)
            }
            setupEventTitleTopMargin(titleTextView = tvTitleParent, post = post)
            tvTitleParent.visible()
        } else {
            tvTitleParent.gone()
        }
    }

    private fun setupParentPostText(post: PostUIEntity) {
        if (isPostsWithBackgroundEnabled && post.parentPost?.isTextWithBackgroundPost().isTrue()) {
            tvTextParent?.gone()
            tvShowMoreText?.gone()
            return
        }
        if (!post.parentPost?.postText.isNullOrEmpty()) {
            tvTextParent?.visible()
            if (post.parentPost?.tagSpan != null) handleInnerPostTagSpans(post)
            else tvTextParent?.setTextNoSpans(post.parentPost?.postText)

            val textColor = ContextCompat.getColor(
                itemView.context,
                if (isVip()) R.color.white_85 else R.color.ui_black
            )
            tvTextParent?.setTextColor(textColor)
        } else {
            tvTextParent?.gone()
        }
        tvTextParent?.setOnClickListener(object : DoubleOrOneClickListener() {
            override fun onClick() {
                postCallback?.onCommentClicked(post, bindingAdapterPosition)
            }
            override fun onDoubleClick() {
                val postEntity = postUIEntity ?: return
                sendLikeWithAnimation(postEntity, R.id.tvText_parent)
            }
        })
        tvShowMoreText?.let { tvShowMoreText ->
            val isImageExists = !post.parentPost?.getImageUrl().isNullOrEmpty()
            tvTextParent?.let { trimPostLength(it, tvShowMoreText, isImageExists) }
        }
        val moreTextColor = if (isVip()) R.color.ui_yellow_tint else R.color.ui_purple_100
        tvShowMoreText?.setTextColor(ContextCompat.getColor(itemView.context, moreTextColor))
        tvShowMoreText?.setOnClickListener(
            object : DoubleOrOneClickListener() {
                override fun onClick() {
                    postCallback?.onShowMoreRepostClicked(post, bindingAdapterPosition)
                }
                override fun onDoubleClick() {
                    sendLikeWithAnimation(post, R.id.tvText_parent)
                }
            }
        )
    }

    private fun setupParentTextBackground(post: PostUIEntity) {
        val isTextBackgroundVisible =
            isPostsWithBackgroundEnabled && post.parentPost?.isTextWithBackgroundPost().isTrue()
        vTextBackground?.isVisible = isTextBackgroundVisible
        if (isTextBackgroundVisible) {
            post.parentPost?.let { parentPost ->
                vTextBackground?.post {
                    vTextBackground.bind(parentPost)
                    setupParentPostTextOnBackground(post)
                }
            }
        }
    }

    private fun setupParentPostTextOnBackground(post: PostUIEntity) {
        val textView = vTextBackground?.getTextView()
        if (post.parentPost?.tagSpan != null) handleInnerPostTagSpans(post, post.parentPost.isWhiteFont().isTrue())
        else textView?.setTextNoSpans(post.parentPost?.postText)

        val textColorLink = ContextCompat.getColor(
            itemView.context,
            if (post.isWhiteFont()) R.color.ui_yellow else R.color.ui_post_text_link
        )
        textView?.setLinkTextColor(textColorLink)

        textView?.setOnClickListener(object : DoubleOrOneClickListener() {
            override fun onClick() {
                postCallback?.onCommentClicked(post, bindingAdapterPosition)
            }

            override fun onDoubleClick() {
                val postEntity = postUIEntity ?: return
                sendLikeWithAnimation(postEntity, R.id.tvText_parent)
            }
        })
    }

    private fun hideParentPostIfDeleted(post: PostUIEntity) {
        tvDeletedParentPostLabel?.isVisible = false
        post.parentPost
            ?.deleted
            ?.takeIf { it == 1 } // is deleted
            ?.let { post.user?.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP }
            ?.let { isVipColorNeeded: Boolean -> showDeletedParentPostLabel(isVipColorNeeded) }
            ?.let { musicPlayerCell?.gone() }
    }

    fun trimPostLength(
        postTextView: TextViewWithImages,
        showMoreTextView: TextView,
        isImageExists: Boolean
    ) {
        postTextView.addEllipsizeListener(object : TextViewWithImages.EllipsizeListener {
            override fun ellipsizeStateChanged(ellipsized: Boolean) {
                Timber.d("trimPostLength isEllipsized = $ellipsized")
                if (ellipsized) showMoreTextView.visible()
                else showMoreTextView.gone()
                postTextView.removeEllipsizeListener(this)
            }
        })
        if (isImageExists) {
            postTextView.maxLines = ChatMessagesAdapter.REPOST_TRIM_WITH_IMAGE
        } else {
            postTextView.maxLines = ChatMessagesAdapter.REPOST_TRIM_WITHOUT_IMAGE
        }

        postTextView.ellipsize = TextUtils.TruncateAt.END
    }

    private fun handleInnerPostTagSpans(post: PostUIEntity, isWhiteText: Boolean = false) {
        val isTextBackgroundVisible =
            isPostsWithBackgroundEnabled && post.parentPost?.isTextWithBackgroundPost().isTrue()
        val textView = if (isTextBackgroundVisible) vTextBackground?.getTextView() else tvTextParent
        val linkColor = if (isTextBackgroundVisible) {
            if (isWhiteText) R.color.ui_yellow else R.color.ui_purple
        } else {
            if (isVip()) R.color.colorGoldCB8D else R.color.ui_purple
        }
        textView?.let { tvTextParent ->
            spanTagsText(
                context = itemView.context,
                tvText = tvTextParent,
                post = post.parentPost?.tagSpan,
                linkColor = linkColor
            )
        }
    }

    private fun showDeletedParentPostLabel(useVipColor: Boolean = false) {
        tvShowMoreText?.isGone = true
        tvTextParent?.isGone = true
        tvTitleParent?.isGone = true
        ivPicture?.isGone = true


        val deletedPostLabelColor = ContextCompat.getColor(
            itemView.context,
            if (useVipColor) {
                R.color.deleted_parent_post_label_color_vip
            } else {
                R.color.deleted_parent_post_label_color
            }
        )
        tvDeletedParentPostLabel?.setTextColor(deletedPostLabelColor)
        tvDeletedParentPostLabel?.isVisible = true
    }

}
