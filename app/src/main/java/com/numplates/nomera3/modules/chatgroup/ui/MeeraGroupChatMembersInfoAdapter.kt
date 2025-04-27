package com.numplates.nomera3.modules.chatgroup.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.chatmembers.UserEntity
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellRightElement
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR

private const val DIVIDER_START = 8

class MeeraGroupChatMembersInfoAdapter(
    private val isShowDotsMenuItem: Boolean,
    private val ownUid: Long,
    private val memberItemListener: MeeraChatMemberInfoCallback
): PagedListAdapter<ChatMember, MeeraGroupChatMembersInfoAdapter.MeeraMemberItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraMemberItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.meera_item_member_info_group_chat, parent, false)
        return MeeraMemberItemViewHolder(
            view = view,
            ownUid = ownUid,
            isShowDotsMenuItem = isShowDotsMenuItem,
            callback = memberItemListener
        )
    }

    override fun onBindViewHolder(holder: MeeraMemberItemViewHolder, position: Int) {
        val item = getItem(position)
        val currentListSize = currentList?.size ?: 0
        val isLastItem = currentListSize - 1 == position
        holder.bind(item, isLastItem = isLastItem)
    }

    class MeeraMemberItemViewHolder(
        view: View,
        private val ownUid: Long,
        private val isShowDotsMenuItem: Boolean,
        val callback: MeeraChatMemberInfoCallback
    ) : RecyclerView.ViewHolder(view) {

        private val cell: UiKitCell = view.findViewById(R.id.member_cell)

        fun bind(member: ChatMember?, isLastItem: Boolean) {
            member?.let {
                val user = member.user
                cell.setLeftUserPicConfig(UserpicUiModel(
                    userAvatarUrl = user.avatarSmall,
                    storiesState = storiesState(member)
                ))
                val friendName = user.name ?: String.empty()

                cell.setMarginStartDivider(DIVIDER_START.dp)
                if (user.approved.toBoolean()) {
                    cell.cellTitleVerified = user.approved.toBoolean()
                } else {
                    cell.cellTitleInterestingAuthor = user.topContentMaker.toBoolean()
                }

                cell.setTitleValue(friendName)
                user.uniqueName?.let { cell.setDescriptionValue("@${user.uniqueName}") }
                val city = user.city ?: String.empty()
                if (city.isNotEmpty()) {
                    cell.cellCityText = true
                    cell.setCityValue(city)
                }
                when (member.type) {
                    USER_TYPE_CREATOR -> {
                        cell.setSubtitleValue(itemView.context.getString(R.string.chat_member_type_creator))
                        cell.cellRightElement = CellRightElement.NONE
                    }
                    USER_TYPE_ADMIN -> {
                        cell.setSubtitleValue(itemView.context.getString(R.string.chat_member_type_admin))
                        cell.cellRightElement = CellRightElement.ICON
                        showDotsMenuIfNotMyItem(ownUid, user)
                    }
                    else -> cell.setSubtitleValue(String.empty())
                }
                if (isLastItem) cell.cellPosition = CellPosition.BOTTOM
                if (isShowDotsMenuItem.not()) cell.cellRightElement = CellRightElement.NONE

                cell.setRightIconClickListener { callback.onGroupUserDotsClicked(member) }
                cell.setThrottledClickListener { callback.onAvatarClicked(member) }
            }
        }

        private fun showDotsMenuIfNotMyItem(ownUid: Long, user: UserEntity) {
            cell.cellRightElement = if (user.userId != ownUid) CellRightElement.ICON else CellRightElement.NONE
        }

        private fun storiesState(member: ChatMember): UserpicStoriesStateEnum {
            val moments = member.user.moments
            if (moments?.hasNewMoments.toBoolean()) return UserpicStoriesStateEnum.NEW
            if (moments?.hasMoments.toBoolean()) return UserpicStoriesStateEnum.VIEWED
            return UserpicStoriesStateEnum.NO_STORIES
        }

    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMember>() {
        override fun areItemsTheSame(oldItem: ChatMember, newItem: ChatMember): Boolean {
            return  oldItem.user.userId == newItem.user.userId
        }

        override fun areContentsTheSame(oldItem: ChatMember, newItem: ChatMember): Boolean {
            return oldItem == newItem
        }
    }



}
