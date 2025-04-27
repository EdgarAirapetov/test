package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.meera.core.extensions.visible

class ReactionBottomMenuVerticalItem @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var view: View

    private var lottieReaction: LottieAnimationView? = null
    private var tvReactionName: TextView? = null
    private var tvReactionNumber: TextView? = null
    private var vReactionDivider: View? = null

    private val reactionCounterFormatter = ReactionCounterFormatter(
        context.getString(R.string.thousand_lowercase_label),
        context.getString(R.string.million_lowercase_label),
        oneAllow = true,
        thousandAllow = false
    )

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.reaction_bottom_menu_vertical_item, this)
        lottieReaction = view.findViewById(R.id.lottie_reaction)
        tvReactionName = view.findViewById(R.id.tv_reaction_name)
        tvReactionNumber = view.findViewById(R.id.tv_reaction_number)
        vReactionDivider = view.findViewById(R.id.v_reaction_divider)
    }

    fun setReaction(reactionEntity: ReactionEntity) {
        val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
        lottieReaction?.setAnimation(reactionType.resourceNoBorder)
        tvReactionName?.text = context.getString(reactionType.resourceName)
        tvReactionNumber?.text = reactionCounterFormatter.format(
            value = reactionEntity.count
        )
    }

    fun addDivider() {
        vReactionDivider?.visible()
    }
}