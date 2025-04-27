package com.numplates.nomera3.presentation.view.adapter.newchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toBooleanOrNull
import com.meera.core.extensions.visible
import com.meera.core.utils.text.ageCityFormattedText
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.presentation.view.widgets.VipView


class PagedGroupChatUsersAdapter(private val onGroupUserListener: IOnGroupUsersClicked) :
        PagedListAdapter<ChatMember, RecyclerView.ViewHolder>(diffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val userItemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_chat_user_item, parent, false)
        return UserItemHolder(userItemView, onGroupUserListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        val userItemHolder = holder as UserItemHolder
        userItemHolder.bind(item)
    }

    class UserItemHolder(
            private val view: View,
            private val onGroupUsersListener: IOnGroupUsersClicked
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val itemContainer: ConstraintLayout = view.findViewById(R.id.container_chat_user_item)
        private val ivVipView: VipView = view.findViewById(R.id.iv_group_user_avatar)
        private val tvUserName: TextView = view.findViewById(R.id.tv_group_user_name)
        private val tvUserAge: TextView = view.findViewById(R.id.tv_group_user_age)
        private val tvUserStatus: TextView = view.findViewById(R.id.tv_group_user_status)
        private val uniqueNameTextView: TextView = view.findViewById(R.id.uniqueNameTextView)
        private val ivDotsMenu: ImageView = view.findViewById(R.id.iv_group_user_dots_menu)

        private var member: ChatMember? = null

        fun bind(newChatMember: ChatMember?) {
            newChatMember?.let { chatMember: ChatMember ->
                this.member = chatMember

                tvUserName.text = chatMember.user.name

                val moments = chatMember.user.moments
                ivVipView.setUp(
                        ivVipView.context,
                        chatMember.user.avatarSmall,
                        chatMember.user.type,
                        chatMember.user.color,
                        hasShadow = false,
                        hasMoments = moments?.hasMoments.toBoolean(),
                        hasNewMoments = moments?.hasNewMoments.toBoolean()
                )

                tvUserAge.text = ageCityFormattedText(
                        chatMember.user.birthday,
                        chatMember.user.city
                )

                when (chatMember.type) {
                    USER_TYPE_CREATOR -> {
                        tvUserStatus.text = view.context.getString(R.string.chat_member_type_creator)
                        tvUserStatus.visible()
                    }
                    USER_TYPE_ADMIN -> {
                        tvUserStatus.text = view.context.getString(R.string.chat_member_type_admin)
                        tvUserStatus.visible()
                    }
                    else -> tvUserStatus.gone()
                }

                ivDotsMenu.setOnClickListener {
                    onGroupUsersListener.onGroupUserDotsClicked(chatMember)
                }
                ivVipView.click { onGroupUsersListener.onAvatarClicked(
                    chatMember,
                    ivVipView,
                    chatMember.user.moments?.hasNewMoments?.toBooleanOrNull()
                ) }

                itemContainer.setOnClickListener(this)

                chatMember.user.uniqueName?.let { nonNullUniqueName: String ->
                    val formattedUniqueName = "@$nonNullUniqueName"
                    uniqueNameTextView.text = formattedUniqueName
                    uniqueNameTextView.visible()
                } ?: kotlin.run {
                    uniqueNameTextView.gone()
                }
            }
        }


        override fun onClick(view: View?) {
            onGroupUsersListener.onGroupUserItemClicked(member)
        }
    }


    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<ChatMember>() {

            override fun areItemsTheSame(oldItem: ChatMember, newItem: ChatMember): Boolean =
                    oldItem.user.userId == newItem.user.userId

            override fun areContentsTheSame(oldItem: ChatMember, newItem: ChatMember): Boolean =
                    oldItem == newItem

        }
    }
}
