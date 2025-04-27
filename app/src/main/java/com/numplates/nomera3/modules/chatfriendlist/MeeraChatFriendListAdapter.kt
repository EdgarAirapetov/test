package com.numplates.nomera3.modules.chatfriendlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R

private const val TOP_ITEM_MARGIN_DP = 16
private const val CELL_END_MARGIN = 16
private const val SINGLE_FRIEND = 1

class MeeraChatFriendListAdapter(
    private val onItemClicked: (UserSimple) -> Unit
) : PagedListAdapter<UserSimple, MeeraChatFriendListAdapter.MeeraFriendInfoViewHolder>(FriendDiffUtils) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraFriendInfoViewHolder {
        val viewItemUserCheck = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.meera_item_user_friend_list, parent, false)
        return MeeraFriendInfoViewHolder(viewItemUserCheck)
    }

    override fun onBindViewHolder(holder: MeeraFriendInfoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            friend = item,
            position = position,
            listSize = currentList?.size ?: 0
        )
        item?.also { itemS ->
            holder.itemView.setOnClickListener { onItemClicked(itemS) }
        }
    }

    override fun getItemCount(): Int = currentList?.size ?: 0

    override fun getItem(position: Int): UserSimple? = currentList?.get(position)

    class MeeraFriendInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cell: UiKitCell = view.findViewById(R.id.friend_cell)

        fun bind(friend: UserSimple?, position: Int, listSize: Int) {
            if (friend == null) return
            val isLastItem = position == listSize - 1
            cell.setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = friend.avatarSmall))
            cell.cellTitleVerified = friend.approved.toBoolean()
            when {
                listSize == SINGLE_FRIEND -> setCellPositionWithTopMargins(CellPosition.ALONE, TOP_ITEM_MARGIN_DP)
                position == 0 -> setCellPositionWithTopMargins(CellPosition.TOP, TOP_ITEM_MARGIN_DP)
                isLastItem -> setCellPositionWithTopMargins(CellPosition.BOTTOM, 0)
                else -> setCellPositionWithTopMargins(CellPosition.MIDDLE, 0)
            }

            val friendName = friend.name ?: String.empty()
            cell.setTitleValue(friendName)
            friend.uniqueName?.let { nonNullUniqueName: String ->
                cell.setDescriptionValue("@$nonNullUniqueName")
            }
        }

        private fun setCellPositionWithTopMargins(cellPosition: CellPosition, marginTop: Int) {
            cell.cellPosition = cellPosition
            cell.setMargins(top = marginTop.dp, end = CELL_END_MARGIN.dp)
        }

    }


    private object FriendDiffUtils: DiffUtil.ItemCallback<UserSimple>() {
        override fun areItemsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
            return oldItem == newItem
        }
    }

}
