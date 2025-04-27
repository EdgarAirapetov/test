package com.numplates.nomera3.presentation.view.adapter.newchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.FriendEntity
import com.numplates.nomera3.presentation.view.widgets.VipView


class ChatGroupRecyclerAdapter(
    private val act: Act,
    private val onGroupFriendsListener: OnGroupFriendsListener
) : ListAdapter<FriendEntity, ChatGroupRecyclerAdapter.UserItemViewHolder>(FriendUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val viewItemUserCheck = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_user_group_chat, parent, false)

        return UserItemViewHolder(
            view = viewItemUserCheck,
            act = act,
            onItemClickListener = { onGroupFriendsListener.onFriendSelected(getItem(it)) }
        )
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserItemViewHolder(
        view: View,
        private val act: Act,
        private val onItemClickListener: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(view) {

        private val vipView: VipView = view.findViewById(R.id.vip_view_friend)
        private val tvName: TextView = view.findViewById(R.id.tv_friend_name)
        private val uniqueNameTextView: TextView = view.findViewById(R.id.tv_friend_unique_name)
        private val cbSelectMember: CheckBox = view.findViewById(R.id.cb_member_selected)
        private val clRoot: ConstraintLayout = view.findViewById(R.id.vg_cl_root_group_chat_member)

        init {
            clRoot.setOnClickListener { onItemClickListener.invoke(absoluteAdapterPosition) }
        }

        fun bind(friend: FriendEntity) {
            setupVipView(act, vipView, friend)
            tvName.text = friend.name
            cbSelectMember.isChecked = friend.isChecked
            friend.uniqueName?.let { nonNullUniqueName: String ->
                val formattedUniqueName = "@$nonNullUniqueName"
                uniqueNameTextView.text = formattedUniqueName
                uniqueNameTextView.visible()
            } ?: kotlin.run {
                uniqueNameTextView.gone()
            }
        }

        private fun setupVipView(act: Act, vipView: VipView, user: FriendEntity) {
            vipView.setUp(
                context = act,
                avatarLink = user.avatarSmall,
                accountType = user.type,
                frameColor = user.color,
            )
            vipView.hideHolidayHat()
        }
    }

    class FriendUserDiffCallback : DiffUtil.ItemCallback<FriendEntity>() {
        override fun areItemsTheSame(oldItem: FriendEntity, newItem: FriendEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendEntity, newItem: FriendEntity): Boolean {
            return oldItem == newItem
        }
    }

    interface OnGroupFriendsListener {
        fun onFriendSelected(user: FriendEntity)
    }
}
