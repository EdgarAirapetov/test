package com.numplates.nomera3.modules.reaction.ui.util

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.meera.core.extensions.dp
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlin.math.max

private val HORIZONTAL_MARGIN_OFFSET = (-8).dp

private const val MAX_REACTION_BADGE_LENGTH = 3

class ReactionBadgeViewManager(private val container: ViewGroup) {

    private var reactions: List<ReactionEntity>? = emptyList()

    fun initReactions(newReactions: List<ReactionEntity>) {
        this.reactions = newReactions
        initViewItems(this.reactions)
    }

    fun clearResources() {
        reactions = null
    }

    private fun initViewItems(reactions: List<ReactionEntity>?) {
        reactions?.let { reactionsList ->
            val reversedList = reactionsList.reversed()
            this.reactions = reversedList.sortReactions()
            container.removeAllViews()
            val startCut = max(0, reactionsList.size - MAX_REACTION_BADGE_LENGTH)
            val stopCut = reactionsList.size
            val subList = reactionsList.subList(startCut, stopCut)
            subList.forEachIndexed { index, reaction ->
                addView(reaction, index, index == subList.size - 1)
            }
        }
    }

    private fun List<ReactionEntity>.sortReactions(): List<ReactionEntity> {
        return sortedBy { reactionEntity -> reactionEntity.count }.toMutableList()
    }

    //TODO удалить runCatching после релиза реакций "Доброе утро" и "Спокойной ночи". Сейчас есть краш из-за добавления новых реакций
    private fun addView(reaction: ReactionEntity, position: Int, isLastItem: Boolean) {
        val type = ReactionType.getByString(reaction.reactionType)
        if (type != null) {
            runCatching {
                val view = getView(type, isLastItem)
                container.addView(view, position)
            }
        }
    }

    private fun getView(type: ReactionType, isLastItem: Boolean): ImageView {
        return ImageView(container.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                if (!isLastItem) {
                    marginEnd = HORIZONTAL_MARGIN_OFFSET
                    setImageResource(type.resourceDrawableBitten)
                } else {
                    setImageResource(type.resourceDrawable)
                }
            }
        }
    }
}
