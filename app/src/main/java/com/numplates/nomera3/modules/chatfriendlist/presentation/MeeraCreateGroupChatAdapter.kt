package com.numplates.nomera3.modules.chatfriendlist.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R

class MeeraCreateGroupChatAdapter(
    private val onCreateGroupChatClicked: () -> Unit
): RecyclerView.Adapter<MeeraCreateGroupChatAdapter.MeeraCreateGroupChatViewHolder>() {

    private var itemCount = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraCreateGroupChatViewHolder {
        val viewItemUserCheck = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.meera_item_groupchat_friend_list, parent, false)

        return MeeraCreateGroupChatViewHolder(viewItemUserCheck)
    }

    override fun onBindViewHolder(holder: MeeraCreateGroupChatViewHolder, position: Int) {
        holder.itemView.setThrottledClickListener { onCreateGroupChatClicked.invoke() }
    }

    override fun getItemCount(): Int = itemCount

    fun setVisibility(isVisible: Boolean) {
        itemCount = if (isVisible) 1 else 0
        notifyDataSetChanged()
    }

    class MeeraCreateGroupChatViewHolder(view: View) : RecyclerView.ViewHolder(view)

}
