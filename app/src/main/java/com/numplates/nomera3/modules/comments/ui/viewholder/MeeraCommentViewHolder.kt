package com.numplates.nomera3.modules.comments.ui.viewholder

import android.graphics.Point
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.bold
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.longClick
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingStart
import com.meera.core.extensions.setPaddingTop
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.listeners.DoubleClickListener
import com.meera.core.utils.showCommonError
import com.meera.db.models.message.UniquenameSpanData
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemCommentBinding
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUpdate
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBadge
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBubble
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.UniquenameType
import com.numplates.nomera3.modules.tags.ui.MovementMethod
import com.numplates.nomera3.presentation.utils.spanTagsText
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts
import com.numplates.nomera3.presentation.view.ui.MeeraTextViewWithImages
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.widgets.DOUBLE_CLICK_TIME_DELAY

const val REPLAY_COMMENT_START_MARGIN = 24

const val AUTHOR_COMMENT_START_MARGIN = 16

private val BUBBLE_MARGIN_END = 19.dp
private val BUBBLE_WIDTH = 330.dp
private val BUBBLE_MARGIN_START = 6.dp
private val BUBBLE_MARGIN_TOP = 60.dp
private val BUBBLE_HEIGHT = 70.dp
private val BUBBLE_MARGIN_TOP_MIN = 30.dp
private val SPACE_MARGIN_FOR_DATE = 8.dp
private val SHOW_MORE_PADDING = 4.dp
private const val MESSAGE_MAX_LINE = 100

class MeeraCommentViewHolder(
    private val binding: MeeraItemCommentBinding,
    private val callback: ICommentsActionsCallback?
) : RecyclerView.ViewHolder(binding.root), ISwipeableHolder {
    private var canSwipeToReply = true
    private var model: CommentEntity? = null

    fun bindPayload(updatePayload: Any) {
        when (updatePayload) {
            is MeeraReactionUpdate -> {
                updateDefaultReactionButton(reactionUpdate = updatePayload)
                updateReactionFit()
                val hasReactions = model?.comment?.reactions?.isNotEmpty() == true
                binding.rbList.isVisible = hasReactions
                if (updatePayload.type == MeeraReactionUpdate.Type.Add) {
                    binding.rbList.addReaction(updatePayload.reactionList, false)
                } else {
                    binding.rbList.removeReaction(updatePayload.reactionList, false)
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

        binding.vvCommentAuthorAvatar.translationY = 0F

        val responseUserName = comment.respName.let {
            if (it.isEmpty()) "" else "$it, "
        }

        if (comment.parentId == null) {
            binding.vgCommentBubble.setMargins(top = COMMENT_BUBBLE_TOP_MARGIN_AVATAR.dp)
        }

        val marginStart = if (comment.parentId == null)
            AUTHOR_COMMENT_START_MARGIN
        else AUTHOR_COMMENT_START_MARGIN + REPLAY_COMMENT_START_MARGIN

        val marginTop = COMMENT_TOP_MARGIN

        val marginTopvgCommentBubble = COMMENT_TOP_MARGIN

        binding.vvCommentAuthorAvatar.setMargins(start = marginStart.dp, top = marginTop.dp)

        binding.vgCommentBubble.setMargins(top = marginTopvgCommentBubble.dp)

        binding.vvCommentAuthorAvatar.setConfig(
            UserpicUiModel(
                size = if (comment.parentId == null) UserpicSizeEnum.Size40 else UserpicSizeEnum.Size24,
                userAvatarUrl = comment.avatar
            )
        )

        binding.tvCommentAuthor.text = comment.user.name
        binding.tvCommentAuthor.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = comment.user.approved.toBoolean(),
                interestingAuthor = comment.user.topContentMaker.toBoolean(),
                approvedIconSize = ApprovedIconSize.SMALL
            )
        )

        binding.tvShowMore.gone()
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

        binding.tvCommentDate.text = NTime.timeAgoComment(comment.date)

        comment.text = model.tagSpan?.text

        bindBubbleListeners(comment)
        binding.vvCommentAuthorAvatar.click { callback?.onCommentProfileClick(comment) }
        binding.tvAnswerComment.clickCheckBubble { callback?.onCommentReplyClick(comment) }
        bindDefaultReaction(model)
        bindFlyingReaction(model)
        binding.tvCommentLike.onScreenAnimationShowListener { reactionEntity, anchorViewLocation ->
            callback?.onCommentReactionAppearAnimation(reactionEntity, anchorViewLocation)
        }

        setupDateContraints()
    }

    private fun setupDateContraints() {
        binding.tvCommentText.post {
            val constraintSet = ConstraintSet()

            constraintSet.clone(binding.vgCommentBubble)

            constraintSet.clear(binding.tvCommentDate.id, ConstraintSet.TOP)
            constraintSet.clear(binding.tvCommentDate.id, ConstraintSet.BOTTOM)

            val layout = binding.tvCommentText.layout
            if (layout != null) {
                val lastLineIndex = layout.lineCount - 1
                val lastLineWidth = layout.getLineWidth(lastLineIndex)
                val totalTextCommentWidth = binding.vgCommentBubble.width - 2 * binding.vgCommentBubble.marginStart
                val dateWidth = binding.tvCommentDate.width
                val isMoreVisible = binding.tvCommentText.isEllipsized()
                val currentWidthWithDate = lastLineWidth + dateWidth + SPACE_MARGIN_FOR_DATE
                val isViewFitsIntoRow = currentWidthWithDate <= totalTextCommentWidth && !isMoreVisible

                if (isViewFitsIntoRow) {
                    constraintSet.connect(
                        binding.tvCommentDate.id,
                        ConstraintSet.BOTTOM,
                        binding.tvCommentText.id,
                        ConstraintSet.BOTTOM
                    )
                } else {
                    constraintSet.connect(
                        binding.tvCommentDate.id,
                        ConstraintSet.TOP,
                        binding.tvCommentText.id,
                        ConstraintSet.BOTTOM
                    )
                }
            } else {
                constraintSet.connect(
                    binding.tvCommentDate.id,
                    ConstraintSet.BOTTOM,
                    binding.tvCommentText.id,
                    ConstraintSet.BOTTOM
                )
            }

            constraintSet.applyTo(binding.vgCommentBubble)
        }
    }

    private fun setupReplyButton(needToShowReplyBtn: Boolean?) {
        canSwipeToReply = if (needToShowReplyBtn == true) {
            binding.tvAnswerComment.visible()
            true
        } else {
            binding.tvAnswerComment.gone()
            false
        }
    }

    private fun handleSpanClicks(clickType: SpanDataClickType) {
        model?.comment?.let { comment ->
            setDoubleClickListener(comment)
        }

        when (clickType) {
            is SpanDataClickType.ClickUserId -> {
                callback?.onCommentMention(clickType.userId ?: return)
            }

            is SpanDataClickType.ClickUnknownUser -> {
                showCommonError(binding.root.context.getText(R.string.uniqname_unknown_profile_message), binding.root)
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
                    tvText = binding.tvCommentText,
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

            else -> Unit
        }
    }

    private fun bindBubbleListeners(comment: CommentEntityResponse) {
        binding.vgCommentBubble.longClickCheckBubble {
            callback?.onCommentLongClick(comment, bindingAdapterPosition)
        }
        binding.tvCommentText.longClickCheckBubble {
            callback?.onCommentLongClick(comment, bindingAdapterPosition)
        }
        setDoubleClickListener(comment)
    }

    private fun setDoubleClickListener(comment: CommentEntityResponse) {
        binding.vgCommentBubble.postDelayed({
            binding.vgCommentBubble.setOnClickListener(createDoubleTapListener(comment))
        }, DOUBLE_CLICK_TIME_DELAY)
        binding.tvCommentText.postDelayed({
            binding.tvCommentText.setOnClickListener(createDoubleTapListener(comment))
        }, DOUBLE_CLICK_TIME_DELAY)
    }

    private fun resetDoubleClickListener() {
        binding.vgCommentBubble.setOnClickListener(null)
        binding.tvCommentText.setOnClickListener(null)
    }

    private fun bindDefaultReaction(model: CommentEntity) {
        val comment = model.comment
        val reactionType = comment.reactions.getMyReaction()
        with(binding.tvCommentLike) {
            setCommentType()
            if (reactionType == null) {
                setLikeButtonState(MeeraContentActionBar.ContentActionBarType.DEFAULT,
                    reactionEntities = listOf(), false)
            }
            click {
                if (comment.reactions.getMyReaction() != null) {
                    setNoneReaction()
                }
                callback?.onCommentLikeClick(comment)
            }
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
        setDefaultReactionType(reactionType, false)
    }

    private fun bindFlyingReaction(model: CommentEntity) {
        model.flyingReactionType?.let { flyingReactionType ->
            with(binding.flyingReaction) {
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
                    binding.tvCommentText.strBuilder = text
                    binding.tvCommentText.movementMethod = MovementMethod
                    binding.tvCommentText.text = text
                    binding.tvCommentText.maxLines = MESSAGE_MAX_LINE
                    binding.tvCommentText.visible()
                } else binding.tvCommentText.gone()
            } else {
                if (comment.text != null && comment.text?.isNotEmpty() == true) {
                    val text = SpannableStringBuilder(responseUserName).append(txt)
                    if (responseUserName.isNotEmpty()) text.bold(0..responseUserName.length)
                    binding.tvCommentText.strBuilder = text
                    binding.tvCommentText.movementMethod = MovementMethod
                    binding.tvCommentText.text = text
                    binding.tvCommentText.visible()
                    trimPostLength(binding.tvCommentText, binding.tvShowMore, model)
                } else binding.tvCommentText.gone()
            }
        }
    }

    private fun bindReactionBadge(model: CommentEntity) {
        binding.rbAdd.setListener { event ->
            val margin = itemView.globalVisibleRect.right - BUBBLE_WIDTH - BUBBLE_MARGIN_END
            val showBubbleAction = { isMoveUpAnimationEnabled: Boolean ->
                showBubble(
                    binding.rbAdd,
                    model = model,
                    isMoveUpAnimationEnabled = isMoveUpAnimationEnabled,
                    margin = margin
                )
            }
            when (event) {
                is MeeraReactionBadge.Event.Tap -> {
                    showBubbleAction(false)
                    binding.rbAdd.context.lightVibrate()
                }

                is MeeraReactionBadge.Event.LongClick -> {
                    showBubbleAction(true)
                    preventRippleFreezeWhenLongTap()
                    binding.rbAdd.context.lightVibrate()
                }
            }
        }

        with(binding.rbList) {
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
        binding.rbAdd.isPressed = false
        binding.tvCommentLike.isPressed = false
    }

    private fun trimPostLength(
        postTextView: MeeraTextViewWithImages,
        showMoreTextView: TextView, model: CommentEntity
    ) {
        postTextView.apply {
            maxLines = COMMENT_TEXT_TRIM
            ellipsize = TextUtils.TruncateAt.END
        }

        showMoreTextView.click {
            postTextView.maxLines = MESSAGE_MAX_LINE
            model.isShowFull = true
            showMoreTextView.gone()
        }

        postTextView.post {
            if (postTextView.isEllipsized()) {
                showMoreTextView.visible()
                showMoreTextView.click {
                    model.isShowFull = true
                    showMoreTextView.gone()
                    postTextView.maxLines = MESSAGE_MAX_LINE
                }
                positioningMoreTextView(postTextView, showMoreTextView)
            } else {
                showMoreTextView.gone()
            }
        }
    }

    private fun positioningMoreTextView(postTextView: MeeraTextViewWithImages,
                                        showMoreTextView: TextView) {
        postTextView.post {
            val layout = postTextView.layout ?: return@post
            val lastLineIndex = postTextView.lineCount - 1

            val lastLineEnd = layout.getLineRight(lastLineIndex)
            val lastLineBaseline = layout.getLineBaseline(lastLineIndex)
            val lastLineTop = layout.getLineTop(lastLineIndex)
            val lastLineBottom = layout.getLineBottom(lastLineIndex)
            val lineHeight = lastLineBottom - lastLineTop

            val postLocation = IntArray(2)
            postTextView.getLocationOnScreen(postLocation)
            val parentLocation = IntArray(2)
            binding.vgCommentBubble.getLocationOnScreen(parentLocation)

            val baseOffsetX = postLocation[0] - parentLocation[0]
            var offsetX = baseOffsetX + lastLineEnd

            var desiredBaseline = (postLocation[1] - parentLocation[1] + lastLineBaseline)

            showMoreTextView.post {
                val showMoreWidth = showMoreTextView.width
                val showMoreBaseline = showMoreTextView.baseline

                var newOffsetY = desiredBaseline - showMoreBaseline

                if (offsetX + showMoreWidth > binding.vgCommentBubble.width) {
                    offsetX = baseOffsetX.toFloat()
                    desiredBaseline += lineHeight
                    newOffsetY = desiredBaseline - showMoreBaseline

                    showMoreTextView.setPaddingStart(0)
                    showMoreTextView.setPaddingTop(SHOW_MORE_PADDING)
                } else {
                    showMoreTextView.setPaddingStart(SHOW_MORE_PADDING)
                    showMoreTextView.setPaddingTop(0)
                }

                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.vgCommentBubble)
                constraintSet.clear(showMoreTextView.id, ConstraintSet.START)
                constraintSet.clear(showMoreTextView.id, ConstraintSet.TOP)

                constraintSet.connect(
                    showMoreTextView.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    offsetX.toInt()
                )
                constraintSet.connect(
                    showMoreTextView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    newOffsetY
                )

                constraintSet.applyTo(binding.vgCommentBubble)
            }
        }
    }

    private fun updateDefaultReactionButton(reactionUpdate: MeeraReactionUpdate) {
        val isAddType = reactionUpdate.type == MeeraReactionUpdate.Type.Add
        if (isAddType) {
            val reactionType = reactionUpdate.reaction
            setDefaultReactionType(reactionType, animate = true)
        } else {
            setNoneReaction()
        }
    }

    private fun setNoneReaction() {
        binding.tvCommentLike.setLikeButtonState(MeeraContentActionBar.ContentActionBarType.DEFAULT,
            reactionEntities = listOf(), false)
    }

    private fun setDefaultReactionType(reactionType: ReactionType, animate: Boolean = false) {
        binding.tvCommentLike.setLikeButtonState(MeeraContentActionBar.ContentActionBarType.DEFAULT,
            reactionEntities = listOf(ReactionEntity(1, 1, reactionType.value)), animate)
    }

    private fun updateReactionFit() {
        itemView.onMeasured {
            val reactionBadgeRect = binding.rbList.globalVisibleRect
            val tvAnswerCommentRect = binding.tvAnswerComment.globalVisibleRect
            val canFit = reactionBadgeRect.left > tvAnswerCommentRect.right
            binding.rbList.setCounterVisibility(canFit)
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
            reactionTip = binding.reactionTip,
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

    override fun getSwipeContainer() = binding.llCommentContainer

    override fun canSwipe() = canSwipeToReply
}
