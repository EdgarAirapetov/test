package com.numplates.nomera3.modules.feed.ui.viewholder

import android.graphics.Point
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingTop
import com.meera.core.extensions.visible
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.meera.db.models.message.ParsedUniquename
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.ASPECT_16x9
import com.numplates.nomera3.MAX_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.holidays.ui.processHolidayText
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelEvent
import com.numplates.nomera3.modules.posts.ui.model.CommunityLabelUiModel
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderNavigationMode
import com.numplates.nomera3.modules.posts.ui.model.PostHeaderUiModel
import com.numplates.nomera3.modules.posts.ui.view.CommunityLabelView
import com.numplates.nomera3.modules.posts.ui.view.MeeraPostHeaderView
import com.numplates.nomera3.modules.posts.ui.view.PostHeaderView
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.presentation.utils.setTextNoSpans
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtilImpl

private const val EVENT_TEXT_TOP_MARGIN_DP = 3

abstract class MeeraBaseRepostHolder(
    private val view: View,
    needToShowCommunityLabel: Boolean = true,
    private val isPostsWithBackgroundEnabled: Boolean = false
) : MeeraBasePostHolder(
    view,
    needToShowCommunityLabel,
    isPostsWithBackgroundEnabled
) {

    protected val tvTitleParent: MeeraTextViewWithImages? = view.findViewById(R.id.tv_item_repost_title)
    protected val tvTextParent: MeeraTextViewWithImages? = view.findViewById(R.id.tvText_parent)
    protected val ivMultimediaView: View? = itemView.findViewById(R.id.iv_repost_multimedia_view)
    private val tvShowMoreText: TextView? = view.findViewById(R.id.tv_show_more_text)
    private val tvDeletedParentPostLabel: TextView? = view.findViewById(R.id.tv_deleted_parent_post_label)
    private val vSeparatorBottom: View? = view.findViewById(R.id.v_image_post_separator)
    override val sensitiveContent: ConstraintLayout? = itemView.findViewById(R.id.sensetive_content_repost)
    override val tvSensitiveContentHeader: TextView? = itemView.findViewById(R.id.tv_sensitive_content_header)
    override val tvSensitiveContentDesc: TextView? = itemView.findViewById(R.id.tv_extra_sensitive_content_description)
    override val uiKitButtonShowPost: UiKitButton? = itemView.findViewById(R.id.cv_show_post)
    private val communityLabelView: CommunityLabelView? = itemView.findViewById(R.id.cl_parent_post_header)
    private val postHeaderView: MeeraPostHeaderView? = itemView.findViewById(R.id.phv_parent_post_header)
    private var communityLabelEventListener: ((CommunityLabelEvent) -> Unit)? = null
    private var postHeaderViewEventListener: ((PostHeaderView) -> Unit)? = null
    private var spanDataClickTextListener: ((SpanDataClickType) -> Unit)? = null
    private var containerDoubleClickListener: ((Point) -> Unit)? = null

    protected val SUM_LEFT_RIGHT_MARGIN = 20.dp

    override fun bind(post: PostUIEntity) {
        super.bind(post)
        setupParentPostHeader(post)
        setupParentPostTitle(post)
        setupParentPostText(post)
        setupParentTextBackground(post)
        hideParentPostIfDeleted(post)
        vSeparatorBottom?.visible()
        setupMultimediaIndicator(post)
        setupSensitiveContentSize()
    }

    private fun setupSensitiveContentSize() {
        val post = postUIEntity ?: return
        val aspect = getMediaAspect(post) ?: return
        when {
            aspect >= MAX_ASPECT -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_SMALL_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_SMALL_MARGIN)
                tvSensitiveContentDesc?.gone()
            }

            aspect >= ASPECT_16x9 && aspect < MAX_ASPECT -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                tvSensitiveContentDesc?.gone()
            }

            else -> {
                tvSensitiveContentHeader?.setMargins(top = SENSITIVE_CONTENT_ITEMS_BIG_MARGIN)
                uiKitButtonShowPost?.setMargins(top = SENSITIVE_CONTENT_ITEMS_MEDIUM_MARGIN)
                tvSensitiveContentDesc?.visible()
            }
        }
    }

    override fun setupClickListeners(post: PostUIEntity) {
        ivPicture?.click { postCallback?.onCommentClicked(post, bindingAdapterPosition) }
        setupPostHeaderListener(post)
        setupEventParticipantsListener(post)
        //TODO ROAD_FIX
//        eventParticipantsView?.setActionListener {
//            postCallback?.onCommentClicked(post, bindingAdapterPosition)
//        }
    }

    override fun resetView() {
        super.resetView()
        tvShowMoreText?.gone()
    }

    override fun clearResources() {
        tvTextParent?.clearResources()
        tvTitleParent?.clearResources()
        tvShowMoreText?.text = ""
        tvShowMoreText?.setOnClickListener(null)
        ivPicture?.setOnClickListener(null)
        communityLabelView?.clearResources()
        postHeaderView?.clearResources()
        vTextBackground?.clearResources()
        communityLabelEventListener = null
        postHeaderViewEventListener = null
        spanDataClickTextListener = null
        containerDoubleClickListener = null
        super.clearResources()
    }

    abstract fun getMediaUrl(post: PostUIEntity): String?

    abstract fun getMediaAspect(post: PostUIEntity): Double?

    protected fun setupMultimediaIndicator(post: PostUIEntity) {
        val needBlur = if (post.parentPost != null) isNeedShowBlur(post.parentPost) else false
        val assets = post.parentPost?.assets
        ivMultimediaView?.isVisible = !assets.isNullOrEmpty() && assets.size > 1
            && !post.isParentPostDeleted() && !needBlur
    }

    private fun setupParentPostHeader(post: PostUIEntity) {
        val parentPost = post.parentPost ?: return
        if (parentPost.isCommunityPost()) {
            val communityLabelUiModel = CommunityLabelUiModel(
                post = parentPost,
                isVipTheme = false
            )
            communityLabelView?.bind(communityLabelUiModel)
            communityLabelEventListener = { event ->
                when (event) {
                    is CommunityLabelEvent.CommunityClicked -> postCallback?.onCommunityClicked(
                        communityId = event.communityId,
                        adapterPosition = bindingAdapterPosition
                    )
                }
            }
            communityLabelView?.setEventListener(communityLabelEventListener)
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
        postHeaderViewEventListener = {
            if (!post.isParentPostDeleted()) {
                postCallback?.onPressRepostHeader(post, bindingAdapterPosition)
            }
        }
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
            val textColorLink = ContextCompat.getColor(itemView.context, R.color.uiKitColorForegroundLink)
            tvTitleParent.setLinkTextColor(textColorLink)
            if (post.parentPost.event.tagSpan != null) {
                spanTagsText(
                    context = itemView.context,
                    tvText = tvTitleParent,
                    post = post.parentPost.event.tagSpan,
                    linkColor = R.color.uiKitColorForegroundLink
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
            return
        }
        val textProcessor = TextProcessorUtilImpl(context = view.context)
        spanData = post.parentPost?.tagSpan?.spanData
        if (post.parentPost?.postText?.isNotEmpty() == true) {
            textProcessor.calculateTextLineCount(
                tagSpan = post.parentPost.tagSpan,
                isMedia = post.parentPost.containsMedia() || post.parentPost.isEvent(),
                isInSnippet = false,
                isRepost = true
            )

            post.parentPost.tagSpan?.let { notNullSpan ->
                setupSpanText(notNullSpan)
            } ?: kotlin.run {
                tvTextParent?.setTextNoSpans(post.parentPost?.postText)
            }
            tvTextParent?.processHolidayText(isVip()) {
                postCallback?.onHolidayWordClicked()
            }
            val textColor = ContextCompat.getColor(itemView.context, R.color.ui_black)
            tvTextParent?.setTextColor(textColor)
            val textColorLink = ContextCompat.getColor(itemView.context, R.color.uiKitColorForegroundLink)
            tvTextParent?.setLinkTextColor(textColorLink)
            setTextMargin(post.parentPost)
            tvTextParent?.visible()
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
    }

    private fun setupSpanText(tagSpan: ParsedUniquename, isWhiteText: Boolean = false) {
        val isTextWithBackgroundPost = isPostsWithBackgroundEnabled && postUIEntity?.isTextWithBackgroundPost().isTrue()
        val textView = if (isTextWithBackgroundPost) vTextBackground?.getTextView() else tvTextParent
        val linkColor = if (isTextWithBackgroundPost) {
            if (isWhiteText) R.color.uiKitColorForegroundLightGreen else R.color.uiKitColorForegroundAddNavy
        } else {
            R.color.uiKitColorForegroundLink
        }
        textView?.let { tvText ->
            spanDataClickTextListener = { clickType ->
                if (clickType is SpanDataClickType.ClickMore) {
                    postUIEntity?.let {
                        actionPostDetail(it.parentPost)
                    }
                }
            }
            spanDataClickTextListener?.let { listener ->
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvText,
                    post = tagSpan,
                    linkColor = linkColor,
                    font = if (isTextWithBackgroundPost) textView.typeface else null,
                    click = listener
                )
            }
        }
    }

    private fun setTextMargin(post: PostUIEntity) {
        tvTextParent ?: return
        when {
            isEventsEnabled && post.event != null -> tvTextParent.setPaddingTop(EVENT_TEXT_TOP_MARGIN_DP.dp)
            post.containsAssets() -> tvTextParent?.setPaddingTop(
                tvTextParent.resources.getDimension(R.dimen.margin_vertical_content_general).toInt()
            )

            else -> tvTextParent.setPaddingTop(0)
        }
    }

    private fun actionPostDetail(post: PostUIEntity?) {
        post?.let {
            postCallback?.onShowMoreTextClicked(
                post = post,
                adapterPosition = bindingAdapterPosition,
                isOpenPostDetail = true,
            )
        }
    }

    private fun setupParentTextBackground(post: PostUIEntity) {
        val isTextBackgroundVisible =
            isPostsWithBackgroundEnabled && post.parentPost?.isTextWithBackgroundPost().isTrue()
        vTextBackground?.isVisible = isTextBackgroundVisible
        if (isTextBackgroundVisible) {
            post.parentPost?.let { parentPost ->
                vTextBackground?.post {
                    vTextBackground?.bind(parentPost)
                    setupParentPostTextOnBackground(post)
                }
            }
        }
    }

    private fun setupParentPostTextOnBackground(post: PostUIEntity) {
        val textView = vTextBackground?.getTextView()
        if (post.parentPost?.tagSpan != null) handleInnerPostTagSpans(post)
        else textView?.setTextNoSpans(post.parentPost?.postText)

        val textColorLink = ContextCompat.getColor(itemView.context, R.color.uiKitColorForegroundAddNavy)
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
            ?.let { showDeletedParentPostLabel() }
            ?.let { musicPlayerCell?.gone() }
    }

    private fun handleInnerPostTagSpans(post: PostUIEntity) {
        val isTextBackgroundVisible =
            isPostsWithBackgroundEnabled && post.parentPost?.isTextWithBackgroundPost().isTrue()
        val textView = if (isTextBackgroundVisible) vTextBackground?.getTextView() else tvTextParent
        val linkColor = if (isTextBackgroundVisible) {
            R.color.uiKitColorForegroundAddNavy
        } else {
            R.color.uiKitColorForegroundLink
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

    private fun showDeletedParentPostLabel() {
        tvShowMoreText?.isGone = true
        tvTextParent?.isGone = true
        tvTitleParent?.isGone = true
        ivPicture?.isGone = true
        tvDeletedParentPostLabel?.isVisible = true
    }
}
