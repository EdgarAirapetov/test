package com.numplates.nomera3.modules.feed.ui.adapter

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.isVisibleToUser
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.longClick
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraPostActionBarCustomBinding
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBadge
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect

private const val MEERA_BUBBLE_VIEW_HEIGHT = 48
private const val BUBBLE_VIEW_BOTTOM_MARGIN = 4
private const val BUBBLE_VIEW_LEFT_PADDING = 10
private const val DELAY_BEFORE_FLYING_ANIMATION_MS = 300L
private val BOTTOM_FLYING_REACTION_MARGIN = dpToPx(128)

class MeeraContentActionBar @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val viewId = ReactionHolderViewId.generate()

    private var callbackListener: Listener? = null
    private var params: Params? = null
    private var momentViewsToggleEnabled: Boolean = false
    private var isNeedCommentVibrate: Boolean = true
    private var onScreenAnimationShowListener: ((reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>) -> Unit)? =
        null

    private val binding: MeeraPostActionBarCustomBinding =
        MeeraPostActionBarCustomBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.rbReactions.setPostType()
    }

    fun clearResources() {
        callbackListener = null
        params = null
        onScreenAnimationShowListener = null
        binding.rlbPostActionBarLikeButton.setOnClickListener(null)
        binding.rlbPostActionBarLikeButton.setOnLongClickListener(null)
        binding.rlbPostActionBarLikeButton.clearResources()
        binding.flyingReaction.clearResources()
        binding.rbReactions.clearResources()
        binding.viewCountViewers.clearResources()
        binding.acbPostActionBarComment.clearResources()
        binding.arbPostActionBarRepost.clearResources()
    }

    fun getReactionBadge(): MeeraReactionBadge {
        return binding.rbReactions
    }

    fun getReactionHolderViewId() = viewId

    fun init(
        params: Params?,
        callbackListener: Listener,
        isNeedToShowRepost: Boolean,
        momentViewsToggleEnabled: Boolean = false,
        isNeedCommentVibrate: Boolean = true
    ) {
        if (params == null) return
        if (this.params == params) return

        this.params = params
        this.callbackListener = callbackListener
        this.momentViewsToggleEnabled = momentViewsToggleEnabled
        this.isNeedCommentVibrate = isNeedCommentVibrate
        val contentActionBarType = ContentActionBarType.getType(params)
        binding.arbPostActionBarRepost.isVisible = isNeedToShowRepost
        binding.acbPostActionBarComment.isGone = params.commentsIsHide
        binding.rlbPostActionBarLikeButton.setButtonThemeByContent(contentActionBarType)
        binding.rlbPostActionBarLikeButton.isGone = params.isMoment && params.isMomentAuthor
        onScreenAnimationShowListener = callbackListener::onReactionClickToShowScreenAnimation.also {
            binding.rlbPostActionBarLikeButton.onScreenAnimationShowListener(it)
        }
        updateActionBar(params = params, contentActionBarType = contentActionBarType)

        binding.rlbPostActionBarLikeButton.setLikeButtonState(
            contentActionBarType = contentActionBarType,
            reactionEntities = params.reactions,
            animate = false
        )
    }

    fun update(params: Params?, reactionHolderViewId: ReactionHolderViewId? = null) {
        if (params == null) return
        if (this.params == params) return

        this.params = params

        val animate = reactionHolderViewId?.isTheSame(viewId) ?: false
        val contentActionBarType = ContentActionBarType.getType(params)

        binding.acbPostActionBarComment.isGone = params.commentsIsHide
        binding.arbPostActionBarRepost.isGone = (
            !params.isMoment
                && params.commentsIsHide
                || params.isPrivateGroupPost
            )
        binding.rlbPostActionBarLikeButton.isGone = params.isMoment && params.isMomentAuthor
        updateActionBar(params = params, contentActionBarType = contentActionBarType)

        binding.rlbPostActionBarLikeButton.setLikeButtonState(
            contentActionBarType = contentActionBarType,
            reactionEntities = params.reactions,
            animate = animate
        )
    }

    fun updateValues(
        repostCount: Int?,
        commentCount: Int?,
        reactions: List<ReactionEntity>?
    ) {
        val params = params?.copy(
            commentCount = commentCount ?: 0,
            repostCount = repostCount ?: 0,
            reactions = reactions ?: emptyList()
        )

        update(params)
    }

    fun playFlyingReactions(reactionType: ReactionType) {
        if (params?.reactions.isNullOrEmpty()) return
        delayOnMeasure {
            if (context == null) return@delayOnMeasure
            if (binding.rbReactions.isVisibleToUser()) {
                binding.flyingReaction.setReactionType(reactionType)
                binding.flyingReaction.startAnimationFlying()
            } else {
                val rect = binding.rbReactions.globalVisibleRect
                val flyingReactionParams = Point(
                    rect.left,
                    getScreenHeight() - BOTTOM_FLYING_REACTION_MARGIN
                )
                val flyingReaction = FlyingReaction(context).apply {
                    setViewLayoutParams(flyingReactionParams)
                    setReactionType(reactionType)
                }
                callbackListener?.onFlyingAnimationInitialized(flyingReaction)
            }
        }
    }

    fun resetReactionsAnimation() {
        binding.rlbPostActionBarLikeButton.resetAnimation()
    }

    fun resetReactionBubbleAppearance() {
        binding.rlbPostActionBarLikeButton.cancelLongPress()
    }

    private fun delayOnMeasure(callback: () -> Unit) {
        onMeasured {
            doDelayed(DELAY_BEFORE_FLYING_ANIMATION_MS) { callback.invoke() }
        }
    }

    private fun updateActionBar(params: Params, contentActionBarType: ContentActionBarType) {
        if (params.isMoment) {
            binding.arbPostActionBarRepost.setButtonEnabledAlpha(params.isEnabled)
            binding.acbPostActionBarComment.setButtonEnabledAlpha(params.isEnabled)
        }
        val isLightText = contentActionBarType == ContentActionBarType.BLUR
            || contentActionBarType == ContentActionBarType.DARK
        binding.rbReactions.setReactions(reactions = params.reactions, isLightText = isLightText)
        initReactionsVisibility(params = params)
        updateRepostButton(contentActionBarType = contentActionBarType, count = params.repostCount)
        updateCommentButton(contentActionBarType = contentActionBarType, count = params.commentCount)
        updateViewsButton(params, contentActionBarType)
        checkForCollapseLikeButton()
        initListeners(contentActionBarType)
    }

    private fun initReactionsVisibility(params: Params) {
        val reactionVisibility = params.isMoment && params.isMomentAuthor && params.reactions.isEmpty()
        binding.rbReactions.isVisible = !reactionVisibility
    }

    private fun updateViewsButton(params: Params, contentActionBarType: ContentActionBarType) {
        binding.viewCountViewers.apply {
            if (params.isMoment && params.isMomentAuthor && momentViewsToggleEnabled) {
                visible()
                setButtonThemeByContent(contentActionBarType)
                setViewsCount(params.viewsCount)
            } else {
                gone()
            }
        }
    }

    private fun updateRepostButton(contentActionBarType: ContentActionBarType, count: Int?) {
        binding.arbPostActionBarRepost.setButtonThemeByContent(contentActionBarType)
        binding.arbPostActionBarRepost.setRepostCount(count)
    }

    private fun updateCommentButton(contentActionBarType: ContentActionBarType, count: Int?) {
        binding.acbPostActionBarComment.setButtonThemeByContent(contentActionBarType)
        binding.acbPostActionBarComment.setCommentCount(count)
    }

    private fun checkForCollapseLikeButton() {
        onMeasured {
            val commentsButton = binding.acbPostActionBarComment.globalVisibleRect
            val reactionBadge = binding.rbReactions.globalVisibleRect
            val isIntersect = reactionBadge.intersect(commentsButton)
            val isLikeButtonCollapsed = binding.rlbPostActionBarLikeButton.getLikeButtonCollapsed()
            if (!isLikeButtonCollapsed) {
                binding.rlbPostActionBarLikeButton.setLikeButtonCollapsed(isIntersect)
            }
        }
    }

    private fun initListeners(contentActionBarType: ContentActionBarType) {
        binding.acbPostActionBarComment.onClick(contentActionBarType) {
            if (isNeedCommentVibrate) context.vibrate()
            callbackListener?.onCommentsClick()
        }
        binding.arbPostActionBarRepost.onClick(contentActionBarType) {
            callbackListener?.onRepostClick()
        }
        if (params?.isEnabled == true) {
            binding.rlbPostActionBarLikeButton.setThrottledClickListener {
                callbackListener?.onReactionRegularClick(viewId)
            }
        } else {
            binding.rlbPostActionBarLikeButton.setThrottledClickListener {
                callbackListener?.onReactionButtonDisabledClick()
            }
        }
        binding.rlbPostActionBarLikeButton.longClick {
            this.context.vibrate()
            val viewsToHide = listOf<View>()
            val showPoint = createPointForReactionBubble()
            callbackListener?.onReactionLongClick(
                showPoint = showPoint,
                reactionTip = binding.reactionTip,
                viewsToHide = viewsToHide,
                reactionHolderViewId = viewId
            )
        }
        binding.rbReactions.setListener { reactionBadgeEvent ->
            if (reactionBadgeEvent is MeeraReactionBadge.Event.Tap || reactionBadgeEvent is MeeraReactionBadge.Event.LongClick) {
                context?.lightVibrate()
                callbackListener?.onReactionBadgeClick()
            }
        }

        binding.viewCountViewers.setClickListener {
            context?.lightVibrate()
            callbackListener?.onViewsCountClick()
        }
    }

    private fun createPointForReactionBubble(): Point {
        binding.rlbPostActionBarLikeButton.globalVisibleRect.let { rect ->
            val startForReactionBubble = rect.left - dpToPx(BUBBLE_VIEW_LEFT_PADDING)
            val topForReactionBubble = rect.top - dpToPx(MEERA_BUBBLE_VIEW_HEIGHT + BUBBLE_VIEW_BOTTOM_MARGIN)
            return if (topForReactionBubble - dpToPx(MEERA_BUBBLE_VIEW_HEIGHT) <= 0) {
                Point(startForReactionBubble, rect.bottom + dpToPx(BUBBLE_VIEW_BOTTOM_MARGIN))
            } else {
                Point(startForReactionBubble, topForReactionBubble)
            }
        }
    }

    data class Params(
        val isEnabled: Boolean,
        val reactions: List<ReactionEntity>,
        val userAccountType: Int?,
        val commentCount: Int,
        val viewsCount: Long = 0L,
        val repostCount: Int,
        val commentsIsHide: Boolean = false,
        val isMoment: Boolean,
        val isVideo: Boolean = false,
        val isMomentAuthor: Boolean = false,
        val isPrivateGroupPost: Boolean = false
    )

    /**
     * Класс позволяющий определить какая View является инициатором выставления реакции
     * (чтобы проиграть у этой View анимацию)
     */
    data class ReactionHolderViewId(val value: Int) {
        companion object {
            private var id: Int = 1

            private fun generateId(): Int {
                return id++
            }

            fun empty(): ReactionHolderViewId {
                return ReactionHolderViewId(0)
            }

            fun generate(): ReactionHolderViewId {
                return ReactionHolderViewId(generateId())
            }
        }

        fun isNotEmpty(): Boolean {
            return value != 0
        }

        fun isTheSame(reactionHolderViewId: ReactionHolderViewId): Boolean {
            return this.value == reactionHolderViewId.value && (reactionHolderViewId.isNotEmpty() && this.isNotEmpty())
        }
    }

    enum class ContentActionBarType {

        DEFAULT, DARK, BLUR;

        companion object {

            fun getType(params: Params?): ContentActionBarType {
                return when {
                    params?.isMoment == true || params?.isVideo == true -> BLUR
                    else -> DEFAULT
                }
            }
        }
    }

    interface Listener {
        fun onCommentsClick()
        fun onRepostClick()
        fun onReactionBadgeClick()
        fun onViewsCountClick() = Unit
        fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: ReactionHolderViewId
        )

        fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction)
        fun onReactionRegularClick(reactionHolderViewId: ReactionHolderViewId)
        fun onReactionButtonDisabledClick()
        fun onReactionClickToShowScreenAnimation(
            reactionEntity: ReactionEntity, anchorViewLocation: Pair<Int, Int>
        ) =
            Unit
    }
}
