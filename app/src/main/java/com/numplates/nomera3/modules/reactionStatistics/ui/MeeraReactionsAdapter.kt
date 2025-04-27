package com.numplates.nomera3.modules.reactionStatistics.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity

private const val MARGIN_START_DIVIDER = 16

class MeeraReactionsAdapter(val onUserClicked: (ReactionUserUiEntity) -> Unit) :
    ListAdapter<ReactionUserUiEntity, MeeraReactionsAdapter.ReactionVH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionVH {
        return ReactionVH(parent.inflate(R.layout.meera_item_reaction_user))
    }

    override fun onBindViewHolder(holder: ReactionVH, position: Int) {
        holder.bind(getItem(position), isLastPosition(position))
    }

    private fun isLastPosition(position: Int): Boolean {
        return currentList.lastIndex == position
    }

    inner class ReactionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vReactionUserItem: UiKitCell = itemView.findViewById(R.id.v_reaction_user_item)
        private val ivReaction: ImageView = itemView.findViewById(R.id.iv_reaction)

        fun bind(item: ReactionUserUiEntity, isLastPosition: Boolean) {
            vReactionUserItem.apply {
                setThrottledClickListener {
                    onUserClicked.invoke(item)
                }
                setMarginStartDivider(MARGIN_START_DIVIDER)
                setTitleValue(item.name ?: "")
                enableTopContentAuthorApprovedUser(
                    TopAuthorApprovedUserModel(
                        approved = item.accountApproved,
                        interestingAuthor = item.topContentMaker,
                        approvedIconSize = ApprovedIconSize.SMALL
                    )
                )
                setCityValue("${context.getString(R.string.uniquename_prefix)}${item.username}")
                cellCityText = true
                setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = item.avatar
                    )
                )
                if (item.reaction != null) {
                    ivReaction.visible()
                    ivReaction.setImageResource(item.reaction.resourceDrawable)
                } else {
                    ivReaction.gone()
                }
                if (isLastPosition){
                    cellPosition = CellPosition.BOTTOM
                }
            }
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ReactionUserUiEntity>() {
            override fun areItemsTheSame(oldItem: ReactionUserUiEntity, newItem: ReactionUserUiEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ReactionUserUiEntity, newItem: ReactionUserUiEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
