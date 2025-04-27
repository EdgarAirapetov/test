package com.numplates.nomera3.modules.reactionStatistics.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import com.numplates.nomera3.presentation.view.widgets.VipView

class ReactionsAdapter(val onUserClicked: (ReactionUserUiEntity) -> Unit) :
    ListAdapter<ReactionUserUiEntity, ReactionsAdapter.ReactionVH>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionVH {
        return ReactionVH(parent.inflate(R.layout.item_reaction_user))
    }

    override fun onBindViewHolder(holder: ReactionVH, position: Int) {
        holder.bind(getItem(position), isLastPosition(position))
    }

    private fun isLastPosition(position: Int): Boolean {
        return itemCount - 1 == position
    }

    inner class ReactionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val vvnAvatar: VipView = itemView.findViewById(R.id.vvn_reaction_user_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_reaction_user_name)
        private val tvUsername: TextView = itemView.findViewById(R.id.tv_reaction_user_username)
        private val ivReaction: ImageView = itemView.findViewById(R.id.iv_reaction)
        private val vDivider: View = itemView.findViewById(R.id.v_bottom_divider)

        fun bind(item: ReactionUserUiEntity, isLastPosition: Boolean) {
            itemView.setThrottledClickListener {
                onUserClicked.invoke(item)
            }

            tvName.text = item.name
            tvName.enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    isVip = item.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
                    customIconTopContent = R.drawable.ic_approved_author_gold_10,
                    approvedIconSize = ApprovedIconSize.SMALL,
                    approved = item.accountApproved,
                    interestingAuthor = item.topContentMaker,
                )
            )
            tvUsername.text = "${tvUsername.context.getString(R.string.uniquename_prefix)}${item.username}"
            vvnAvatar.setUp(itemView.context, item.avatar, item.accountType, item.frameColor)

            if (item.reaction != null) {
                ivReaction.visible()
                ivReaction.setImageResource(item.reaction.resourceDrawable)
            } else {
                ivReaction.gone()
            }

            vDivider.isGone = isLastPosition
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
