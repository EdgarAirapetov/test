package com.numplates.nomera3.modules.comments.ui.viewholder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Point
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.bold
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.longClick
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.meera.core.utils.listeners.DoubleClickListener
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.widgets.userpic.UiKitUserpicImage
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_VIP
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUpdate
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBadge
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionLikeButton
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBubble
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.tags.ui.MovementMethod
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.DOUBLE_CLICK_TIME_DELAY

const val COMMENT_START_MARGIN = 20
const val INNER_COMMENT_MARGIN = 40
const val COMMENT_TOP_MARGIN = 6
const val INNER_COMMENT_TOP_MARGIN = 0
const val COMMENT_BUBBLE_TOP_MARGIN_AVATAR = 6
const val INNER_COMMENT_BUBBLE_TOP_MARGIN_AVATAR = 6

const val COMMENT_TEXT_TRIM = 10

const val INNER_COMMENT_START_MARGIN = INNER_COMMENT_MARGIN + COMMENT_START_MARGIN

private val BUBBLE_MARGIN_END = 19.dp
private val BUBBLE_WIDTH = 330.dp
private val BUBBLE_MARGIN_START = 6.dp
private val BUBBLE_MARGIN_TOP = 60.dp
private val BUBBLE_HEIGHT = 70.dp
private val BUBBLE_MARGIN_TOP_MIN = 30.dp

class CommentViewHolder(
    private val view: View,
    private val callback: ICommentsActionsCallback?
) : RecyclerView.ViewHolder(view), ISwipeableHolder {
    private val tvName: TextView = view.findViewById(R.id.tv_comment_author)
    private val ivAvatar: UiKitUserpicImage = view.findViewById(R.id.vv_comment_author_avatar)
    private val tvText: TextViewWithImages = view.findViewById(R.id.tv_comment_text)
    private val tvDate: TextView = view.findViewById(R.id.tv_comment_date)
    private val commentContainer: LinearLayout = view.findViewById(R.id.ll_comment_container)
    private val rbList: MeeraReactionBadge = view.findViewById(R.id.rb_list)
    private val rbAdd: MeeraReactionBadge = view.findViewById(R.id.rb_add)
    private val flyingReaction: FlyingReaction = view.findViewById(R.id.flying_reaction)
    private val commentBubble: ViewGroup = view.findViewById(R.id.vg_comment_bubble)
    private val answerBtn: TextView = view.findViewById(R.id.tv_answer_comment)
    private val tvLike: MeeraReactionLikeButton = view.findViewById(R.id.tv_comment_like)
    private val tvShowMore: TextView = view.findViewById(R.id.tv_show_more)
    private val reactionTip: TextView = view.findViewById(R.id.reaction_tip)
    private val lavLike: LottieAnimationView? = itemView.findViewById(R.id.lav_progress)

    private val lottieLikeAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            lavLike?.takeIf { it.isAnimating }?.cancelAnimation()
            lavLike?.gone()
        }
    }
    private var canSwipeToReply = true
    private var model: CommentEntity? = null

    fun bindPayload(updatePayload: Any) {
        when (updatePayload) {
            is CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation -> view.post {
                lavLike?.visible()
                lavLike?.playAnimation()
            }

            is MeeraReactionUpdate -> {
                updateDefaultReactionButton(reactionUpdate = updatePayload)
                updateReactionFit()
                val hasReactions = model?.comment?.reactions?.isNotEmpty() == true
                rbList.isVisible = hasReactions
                if (updatePayload.type == MeeraReactionUpdate.Type.Add) {
                    rbList.addReaction(updatePayload.reactionList, false)
                } else {
                    rbList.removeReaction(updatePayload.reactionList, false)
                }
            }

            is CommentUpdate -> {
                setupReplyButton(updatePayload.needToShowReplyBtn)
            }
        }
    }

    fun bind(model: CommentEntity) {
        val comment = model.comment
        this.model = model

        bindReactionBadge(model)


        val responseUserName = comment.respName.let {
            if (it.isEmpty()) "" else "$it, "
        }

        if (comment.parentId == null) {
            commentBubble.setMargins(top = COMMENT_BUBBLE_TOP_MARGIN_AVATAR.dp)
        } else {
            commentBubble.setMargins(top = INNER_COMMENT_BUBBLE_TOP_MARGIN_AVATAR.dp)
        }

        val marginStart = if (comment.parentId == null)
            COMMENT_START_MARGIN
        else INNER_COMMENT_START_MARGIN

        val marginTop = if (comment.parentId == null)
            COMMENT_TOP_MARGIN
        else INNER_COMMENT_TOP_MARGIN

        val marginTopCommentBubble = if (comment.parentId == null)
            COMMENT_TOP_MARGIN else if (comment.accountType == ACCOUNT_TYPE_VIP) 6 else 6

        ivAvatar.setMargins(start = marginStart.dp, top = marginTop.dp)

        commentBubble.setMargins(top = marginTopCommentBubble.dp)

        ivAvatar.setConfig(
            UserpicUiModel(
                size = if (comment.parentId == null) UserpicSizeEnum.Size40 else UserpicSizeEnum.Size24,
                userAvatarUrl = comment.user.avatar
            )
        )

        tvName.text = comment.user.name
        tvName.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = comment.user.approved.toBoolean(),
                customIconTopContent = R.drawable.ic_approved_author_gold_10,
                isVip = comment.user.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
                interestingAuthor = comment.user.topContentMaker.toBoolean(),
                approvedIconSize = ApprovedIconSize.SMALL
            )
        )

        tvShowMore.gone()
        val commentContent = if (model.tagSpan != null) {
            spanTagsText(
                context = itemView.context,
                post = model.tagSpan,
                click = ::handleSpanClicks
            )
        } else {
            SpannableStringBuilder(comment.text)
        }
        setTextForComment(
            comment = comment,
            responseUserName = responseUserName,
            txt = commentContent
        )

        setupReplyButton(model.needToShowReplyBtn)

        tvDate.text = NTime.timeAgoComment(comment.date)

        comment.text = model.tagSpan?.text

        lavLike?.addAnimatorListener(lottieLikeAnimationListener)
        bindBubbleListeners(comment)
        ivAvatar.click { callback?.onCommentProfileClick(comment) }
        answerBtn.clickCheckBubble { callback?.onCommentReplyClick(comment) }
        bindDefaultReaction(model)
        bindFlyingReaction(model)
    }

    private fun setupReplyButton(needToShowReplyBtn: Boolean?) {
        canSwipeToReply = if (needToShowReplyBtn == true) {
            answerBtn.visible()
            true
        } else {
            answerBtn.gone()
            false
        }
    }

    private fun handleSpanClicks(clickType: SpanDataClickType) {
        model?.comment?.let { comment ->
            setDoubleClickListener(comment)
        }

        when (clickType) {
            is SpanDataClickType.ClickUserId -> {
                callback?.onCommentMention(clickType.userId ?: return@handleSpanClicks)
            }

            is SpanDataClickType.ClickUnknownUser -> {
                NToast.with(itemView)
                    .text(itemView.context.getString(R.string.uniqname_unknown_profile_message))
                    .show()
            }

            is SpanDataClickType.ClickHashtag -> {
                callback?.onHashtagClicked(clickType.hashtag)
            }

            is SpanDataClickType.ClickBadWord -> {
                resetDoubleClickListener()
                val originText = model?.tagSpan?.text ?: ""
                val badWord = clickType.badWord ?: ""
                model?.tagSpan?.text = originText.replaceRange(
                    startIndex = clickType.startIndex,
                    endIndex = clickType.endIndex,
                    replacement = badWord
                )
                model?.tagSpan?.deleteSpanDataById(clickType.tagSpanId)
                val txt = spanTagsText(
                    context = itemView.context,
                    post = model?.tagSpan,
                    click = ::handleSpanClicks
                )
                model?.tagSpan?.addSpanData(
                    UniquenameSpanData(
                        id = null,
                        tag = null,
                        type = UniquenameType.PROFANITY_NO_LINK.value,
                        startSpanPos = clickType.startIndex,
                        endSpanPos = clickType.startIndex + badWord.length,
                        userId = null,
                        groupId = null,
                        symbol = badWord
                    )
                )

                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvText,
                    post = model?.tagSpan,
                    click = ::handleSpanClicks
                )

                model?.let { model ->
                    val responseUserName = model.comment.respName.let {
                        if (it.isEmpty()) "" else "$it, "
                    }
                    setTextForComment(model.comment, responseUserName, txt)
                }
            }

            is SpanDataClickType.ClickLink -> {
                callback?.onCommentLinkClick(clickType.link)
            }

            else -> {}
        }
    }

    private fun bindBubbleListeners(comment: CommentEntityResponse) {
        commentBubble.longClickCheckBubble {
            callback?.onCommentLongClick(comment, bindingAdapterPosition)
        }
        tvText.longClickCheckBubble {
            callback?.onCommentLongClick(comment, bindingAdapterPosition)
        }
        setDoubleClickListener(comment)
    }

    private fun setDoubleClickListener(comment: CommentEntityResponse) {
        commentBubble.postDelayed({
            commentBubble.setOnClickListener(createDoubleTapListener(comment))
        }, DOUBLE_CLICK_TIME_DELAY)
        tvText.postDelayed({
            tvText.setOnClickListener(createDoubleTapListener(comment))
        }, DOUBLE_CLICK_TIME_DELAY)
    }

    private fun resetDoubleClickListener() {
        commentBubble.setOnClickListener(null)
        tvText.setOnClickListener(null)
    }

    private fun bindDefaultReaction(model: CommentEntity) {
        val comment = model.comment
        val reactionType = comment.reactions.getMyReaction()
        with(tvLike) {
            if (reactionType == null) {
//                text = itemView.context.getString(ReactionType.GreenLight.resourceName)
            }
            click {
                if (comment.reactions.getMyReaction() != null) {
                    setNoneReaction()
                }
                callback?.onCommentLikeClick(comment)
            }
//            textColor(R.color.colorGrey8080)
            longClick {
                itemView.context.lightVibrate()
                preventRippleFreezeWhenLongTap()
                showBubble(
                    actionView = this@with,
                    model = model
                )
            }
        }
        if (reactionType == null) return
        setDefaultReactionType(reactionType)
    }

    private fun bindFlyingReaction(model: CommentEntity) {
        model.flyingReactionType?.let { flyingReactionType ->
            with(flyingReaction) {
                isVisible = true
                setReactionType(flyingReactionType)
                startAnimationFlying()
                model.flyingReactionType = null
            }
        }
    }

    private fun setTextForComment(
        comment: CommentEntityResponse,
        responseUserName: String,
        txt: SpannableStringBuilder
    ) {
        model?.let { model ->
            if (model.isShowFull) {
                if (comment.text != null && comment.text?.isNotEmpty() == true) {
                    val text = SpannableStringBuilder(responseUserName).append(txt)
                    if (responseUserName.isNotEmpty()) text.bold(0..responseUserName.length)
                    tvText.strBuilder = text
                    tvText.movementMethod = MovementMethod
                    tvText.text = text
                    tvText.maxLines = 100
                    tvText.visible()
                } else tvText.gone()
            } else {
                if (comment.text != null && comment.text?.isNotEmpty() == true) {
                    val text = SpannableStringBuilder(responseUserName).append(txt)
                    if (responseUserName.isNotEmpty()) text.bold(0..responseUserName.length)
                    tvText.strBuilder = text
                    tvText.movementMethod = MovementMethod
                    tvText.text = text
                    tvText.visible()
                    trimPostLength(tvText, tvShowMore, model)
                } else tvText.gone()
            }
        }
    }

    private fun bindReactionBadge(model: CommentEntity) {
        rbAdd.setListener { event ->
            val margin = itemView.globalVisibleRect.right - BUBBLE_WIDTH - BUBBLE_MARGIN_END
            val showBubbleAction = { isMoveUpAnimationEnabled: Boolean ->
                showBubble(
                    rbAdd,
                    model = model,
                    isMoveUpAnimationEnabled = isMoveUpAnimationEnabled,
                    margin = margin
                )
            }
            when (event) {
                is MeeraReactionBadge.Event.Tap -> {
                    showBubbleAction(false)
                    rbAdd.context.lightVibrate()
                }

                is MeeraReactionBadge.Event.LongClick -> {
                    showBubbleAction(true)
                    preventRippleFreezeWhenLongTap()
                    rbAdd.context.lightVibrate()
                }
            }
        }

        with(rbList) {
            isVisible = model.comment.reactions.isNotEmpty()
            initReactions(
                reactions = model.comment.reactions,
                isLightText = false
            )
            setListener { event ->
                if (event is MeeraReactionBadge.Event.Tap || event is MeeraReactionBadge.Event.LongClick) {
                    context?.lightVibrate()
                    callback?.onReactionBadgeClick(model.comment)
                }
            }
        }
    }

    private fun preventRippleFreezeWhenLongTap() {
        rbAdd.isPressed = false
        tvLike.isPressed = false
    }

    private fun trimPostLength(
        postTextView: TextViewWithImages,
        showMoreTextView: TextView, model: CommentEntity
    ) {
        postTextView.apply {
            maxLines = COMMENT_TEXT_TRIM
            ellipsize = TextUtils.TruncateAt.END
        }

        showMoreTextView.click {
            postTextView.maxLines = 100
            model.isShowFull = true
            showMoreTextView.gone()
        }

        postTextView.post {
            if (postTextView.isEllipsized()) {
                showMoreTextView.visible()
                showMoreTextView.click {
                    model.isShowFull = true
                    showMoreTextView.gone()
                    postTextView.maxLines = 100
                }
            } else {
                showMoreTextView.gone()
            }
        }
    }

    private fun updateDefaultReactionButton(reactionUpdate: MeeraReactionUpdate) {
        val isAddType = reactionUpdate.type == MeeraReactionUpdate.Type.Add
        if (isAddType) {
            val reactionType = reactionUpdate.reaction
            setDefaultReactionType(reactionType)
        } else {
            setNoneReaction()
        }
    }

    private fun setNoneReaction() {
        tvLike.setBackgroundResource(0)
    }

    private fun setDefaultReactionType(reactionType: ReactionType) {
        tvLike.setBackgroundResource(reactionType.resourceBackground)
    }

    private fun updateReactionFit() {
        itemView.onMeasured {
            val reactionBadgeRect = rbList.globalVisibleRect
            val answerBtnRect = answerBtn.globalVisibleRect
            val canFit = reactionBadgeRect.left > answerBtnRect.right
            rbList.setCounterVisibility(canFit)
        }
    }

    private fun createDoubleTapListener(comment: CommentEntityResponse) =
        object : DoubleClickListener() {
            override fun onDoubleClick() {
                if (comment.reactions.getMyReaction() != ReactionType.GreenLight) {
                    callback?.onCommentDoubleClick(comment)
                } else {
                    itemView.context?.lightVibrate()
                    callback?.onCommentPlayClickAnimation(comment.id)
                }
            }
        }

    private fun showBubble(
        actionView: View,
        model: CommentEntity,
        isMoveUpAnimationEnabled: Boolean = true,
        margin: Int = BUBBLE_MARGIN_START
    ) {
        val top = actionView.globalVisibleRect.top
        val y = if (top - BUBBLE_MARGIN_TOP > BUBBLE_HEIGHT) {
            top - BUBBLE_MARGIN_TOP
        } else {
            top + BUBBLE_MARGIN_TOP_MIN
        }
        val showPoint = Point(margin, y)
        callback?.onCommentShowReactionBubble(
            commentId = model.id,
            commentUserId = model.comment.user.userId,
            showPoint = showPoint,
            viewsToHide = emptyList(),
            reactionTip = reactionTip,
            currentReactionsList = model.comment.reactions,
            isMoveUpAnimationEnabled = isMoveUpAnimationEnabled
        )
    }

    private fun View.clickCheckBubble(click: (View) -> Unit) {
        click { view ->
            if (isBubbleNotExist()) {
                click(view)
            }
        }
    }

    private fun View.longClickCheckBubble(click: (View) -> Unit) {
        longClick { view ->
            if (isBubbleNotExist()) {
                click(view)
            }
        }
    }

    private fun isBubbleNotExist(): Boolean {
        val act = itemView.context as? Act ?: return true
        val bubble = (act.getRootView() as? ViewGroup)?.children?.find { it is ReactionBubble } as? ReactionBubble
        return bubble == null
    }

    override fun getSwipeContainer() = commentContainer

    override fun canSwipe() = canSwipeToReply
}
