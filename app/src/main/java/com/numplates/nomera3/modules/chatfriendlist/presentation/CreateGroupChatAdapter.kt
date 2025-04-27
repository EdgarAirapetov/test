package com.numplates.nomera3.modules.chatfriendlist.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R

class CreateGroupChatAdapter(
    private val onCreateGroupChatClicked: () -> Unit
) : RecyclerView.Adapter<CreateGroupChatAdapter.CreateGroupChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupChatViewHolder {
        val viewItemUserCheck = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_groupchat_friend_list, parent, false)

        return CreateGroupChatViewHolder(viewItemUserCheck)
    }

    override fun onBindViewHolder(holder: CreateGroupChatViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onCreateGroupChatClicked.invoke() }
    }

    override fun getItemCount(): Int = 1

    class CreateGroupChatViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
