package com.numplates.nomera3.modules.chatgroup.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.FriendEntity

private const val LAST_ITEM_MARGIN_BOTTOM = 112

class MeeraGroupChatListMembersAdapter(
    private val onGroupFriendsListener: MeeraOnGroupMembersListener
): ListAdapter<FriendEntity, MeeraGroupChatListMembersAdapter.MeeraUserItemViewHolder>(MeeraMemberUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraUserItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.meera_item_member_group_chat, parent, false)
        return MeeraUserItemViewHolder(
            view = view,
            onItemClickListener = { onGroupFriendsListener.onMemberSelected(getItem(it)) }
        )
    }

    override fun onBindViewHolder(holder: MeeraUserItemViewHolder, position: Int) {
        addBottomMarginToLastItem(holder, position)
        val item = getItem(position)
        val isLastItem = currentList.size - 1 == position
        holder.bind(item, isLastItem = isLastItem)
    }

    private fun addBottomMarginToLastItem(holder: MeeraUserItemViewHolder, position: Int) {
        if (position == currentList.size - 1) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = LAST_ITEM_MARGIN_BOTTOM.dp
            holder.itemView.setLayoutParams(params)
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 0
            holder.itemView.setLayoutParams(params)
        }
    }

    class MeeraUserItemViewHolder(
        view: View,
        private val onItemClickListener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val cell: UiKitCell = view.findViewById(R.id.member_cell)

        init {
            itemView.setThrottledClickListener { onItemClickListener.invoke(absoluteAdapterPosition) }
        }

        fun bind(friend: FriendEntity, isLastItem: Boolean) {
            cell.setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = friend.avatarSmall))
            val friendName = friend.name ?: String.empty()
            cell.cellTitleVerified = friend.approved.toBoolean()
            cell.setTitleValue(friendName)
            friend.uniqueName?.let {
                cell.setDescriptionValue("@${friend.uniqueName}")
            }
            cell.setCellRightElementChecked(isChecked = friend.isChecked)
            cell.cellRightIconClickListener = { onItemClickListener.invoke(absoluteAdapterPosition) }
            if (isLastItem) cell.cellPosition = CellPosition.BOTTOM
        }

    }

    interface MeeraOnGroupMembersListener {
        fun onMemberSelected(user: FriendEntity)
    }

    private class MeeraMemberUserDiffCallback : DiffUtil.ItemCallback<FriendEntity>() {
        override fun areItemsTheSame(oldItem: FriendEntity, newItem: FriendEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendEntity, newItem: FriendEntity): Boolean {
            return oldItem == newItem
        }
    }


}
