package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.textColor
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraReactionBadgeBinding
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.util.ReactionBadgeViewManager
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount

private val MARGIN_START_REACTIONS_BADGE = 3.dp

class MeeraReactionBadge @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val addButtonClosed =
        ContextCompat.getDrawable(context, R.drawable.ic_reaction_add)

    private val addButtonOpen =
        ContextCompat.getDrawable(context, R.drawable.ic_reaction_add_selected)

    private val binding: MeeraReactionBadgeBinding =
        MeeraReactionBadgeBinding.inflate(LayoutInflater.from(context), this, true)

    private var type = Type.Comments

    private var listener: ((Event) -> Unit)? = null
    private var isOpened: Boolean = false
    private var isVisibleDefault: Boolean = true
    private var reactions: MutableList<ReactionEntity> = mutableListOf()
    private var viewManager: ReactionBadgeViewManager? = null

    private val reactionCounterFormatter = ReactionCounterFormatter(
        thousandLabel = context.getString(R.string.thousand_lowercase_label),
        millionLabel = context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    init {
        binding.ivAddReactionButton.setThrottledClickListener {
            listener?.invoke(Event.Tap)
        }
        binding.llReactionBadge.setThrottledClickListener {
            listener?.invoke(Event.Tap)
        }
        binding.ivAddReactionButton.longClick {
            listener?.invoke(Event.LongClick)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOpened(isOpened)
    }

    fun setPostType() {
        type = Type.Post
        binding.ivAddReactionButton.gone()
    }

    fun initReactions(
        reactions: List<ReactionEntity>,
        isLightText: Boolean
    ) {
        setReactions(
            reactions = reactions,
            isLightText = isLightText
        )
    }

    fun setReactions(reactions: List<ReactionEntity>, isLightText: Boolean) {
        this.reactions = reactions.toMutableList()
        val showReactionButton = this.reactions.isEmpty()
        val counterView = getCounterView()
        if (type == Type.Comments) {
            binding.ivAddReactionButton.setVisible(isVisibleDefault && showReactionButton)
            binding.llReactionBadgeContainer.setMargins(MARGIN_START_REACTIONS_BADGE, 0, 0, 0)
        }
        initCounterColor(counterView = counterView, isLightText = isLightText)
        counterView.setVisible(!showReactionButton)
        getViewManager()?.initReactions(this.reactions)
        setCounterText(counterView, reactions.reactionCount())
    }

    private fun getViewManager(): ReactionBadgeViewManager? {
        if (viewManager == null) {
            viewManager = ReactionBadgeViewManager(binding.llReactionBadgeContainer)
        }
        return viewManager
    }

    fun setCounterVisibility(isVisible: Boolean) {
        val counterView = getCounterView()
        counterView.setVisible(isVisible)
    }

    fun addReaction(reactions: List<ReactionEntity>, isLightText: Boolean) {
        setReactions(reactions = reactions, isLightText = isLightText)
    }

    fun removeReaction(reactions: List<ReactionEntity>, isLightText: Boolean) {
        setReactions(reactions = reactions, isLightText = isLightText)
    }

    fun setDefaultVisibility(isVisible: Boolean) {
        isVisibleDefault = isVisible
    }

    fun setListener(listener: (Event) -> Unit) {
        this.listener = listener
    }

    fun setOpened(value: Boolean) {
        val drawable = if (value) {
            addButtonOpen
        } else {
            addButtonClosed
        }
        if (drawable != null) {
            setButtonAddIcon(drawable)
        }
        isOpened = value
    }

    fun clearResources(){
        binding.ivAddReactionButton.setImageDrawable(null)
        viewManager?.clearResources()
        viewManager = null
        reactions.clear()
        listener = null
    }

    private fun initCounterColor(counterView: TextView, isLightText: Boolean) {
        if (isLightText) {
            counterView.textColor(R.color.color_action_bar_light_text_color)
        } else {
            counterView.textColor(R.color.uiKitColorLegacySecondary)
        }
    }

    private fun setCounterText(counterView: TextView, value: Int) {
        val counterText = reactionCounterFormatter.format(value)
        counterView.text = counterText
    }

    private fun getCounterView(): TextView {
        val isCommentsType = type == Type.Comments
        binding.tvReactionBadgeCounterLeft.setVisible(isCommentsType)
        binding.tvReactionBadgeCounterRight.setVisible(!isCommentsType)
        return if (isCommentsType) {
            binding.tvReactionBadgeCounterLeft
        } else {
            binding.tvReactionBadgeCounterRight
        }
    }

    private fun setButtonAddIcon(drawable: Drawable) {
        binding.ivAddReactionButton.setImageDrawable(drawable)
    }

    sealed class Event {
        object Tap : Event()
        object LongClick : Event()
    }

    enum class Type {
        Comments,
        Post
    }
}
