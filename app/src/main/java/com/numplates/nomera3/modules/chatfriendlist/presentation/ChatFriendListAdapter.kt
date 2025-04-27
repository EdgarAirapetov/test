package com.numplates.nomera3.modules.chatfriendlist.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.numplates.nomera3.presentation.view.widgets.VipView

class ChatFriendListAdapter(
    private val onItemClicked: (UserSimple) -> Unit
) : PagedListAdapter<UserSimple, ChatFriendListAdapter.FriendInfoViewHolder>(FriendDiffUtils) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendInfoViewHolder {
        val viewItemUserCheck = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_user_friend_list, parent, false)

        return FriendInfoViewHolder(viewItemUserCheck)
    }

    override fun onBindViewHolder(holder: FriendInfoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        item?.also { itemS ->
            holder.itemView.setOnClickListener { onItemClicked(itemS) }
        }
    }

    override fun getItemCount(): Int = currentList?.size ?: 0

    override fun getItem(position: Int): UserSimple? = currentList?.get(position)

    class FriendInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val avatarVipView: VipView = view.findViewById(R.id.vip_view_friend)
        private val nameTv: TextView = view.findViewById(R.id.tv_friend_name)
        private val uniqueNameTv: TextView = view.findViewById(R.id.tv_friend_unique_name)

        fun bind(friend: UserSimple?) {
            if (friend == null) return

            setupVipView(avatarVipView, friend)
            nameTv.text = friend.name
            friend.uniqueName?.let { nonNullUniqueName: String ->
                val formattedUniqueName = "@$nonNullUniqueName"
                uniqueNameTv.text = formattedUniqueName
                uniqueNameTv.visible()
            } ?: kotlin.run {
                uniqueNameTv.gone()
            }
        }

        private fun setupVipView(vipView: VipView, user: UserSimple) {
            vipView.setUp(
                context = vipView.context,
                avatarLink = user.avatarSmall,
                accountType = user.accountType,
                frameColor = user.accountColor
            )
            vipView.hideHolidayHat()
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
