package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel.BlacklistClearButtonUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel.BlacklistHeaderUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel.BlacklistedMemberUIModel
import timber.log.Timber
import kotlin.properties.Delegates

class CommunityBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val RESERVED_LIST_SIZE = 2
    }

    val blacklist: MutableList<CommunityBlacklistUIModel> by Delegates.observable(
        mutableListOf(BlacklistHeaderUIModel(0))
    ) { _, _, newValue ->
        onDataSetChanged?.invoke(newValue.size)
    }

    var onDataSetChanged: ((Int) -> Unit)? = null
    var blacklistItemClickListener: ((BlacklistedMemberUIModel?) -> Unit)? = null
    var clearBlacklistClickListener: (() -> Unit)? = null
    var blacklistContextMenuClickListener: ((BlacklistedMemberUIModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.blacklist_community_delete_all_item -> CommunityBlacklistDeleteAllItemViewHolder(parent)
            R.layout.blacklist_community_length_item -> CommunityBlacklistLengthItemViewHolder(parent)
            R.layout.blacklist_community_member_item -> CommunityBlacklistMemberItemViewHolder(parent)
            else -> CommunityBlacklistEmptyItemViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val blacklistItem = blacklist[position]
        when (holder) {
            is CommunityBlacklistDeleteAllItemViewHolder -> holder.bind(clearBlacklistClickListener)
            is CommunityBlacklistLengthItemViewHolder -> holder.bind(blacklistItem as? BlacklistHeaderUIModel)
            is CommunityBlacklistMemberItemViewHolder -> holder.bind(
                blacklistItem as BlacklistedMemberUIModel,
                blacklistItemClickListener,
                blacklistContextMenuClickListener
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (blacklist[position]) {
            BlacklistClearButtonUIModel -> R.layout.blacklist_community_delete_all_item
            is BlacklistHeaderUIModel -> R.layout.blacklist_community_length_item
            is BlacklistedMemberUIModel -> R.layout.blacklist_community_member_item
        }
    }

    override fun getItemCount() = blacklist.size

    fun addItemList(newItems: List<CommunityBlacklistUIModel>) {
        if (blacklist.last() is BlacklistClearButtonUIModel) {
            val clearButton = blacklist.removeAt(blacklist.lastIndex)
            blacklist.addAll(newItems)
            blacklist.add(clearButton)
        } else {
            blacklist.addAll(newItems)
            blacklist.add(BlacklistClearButtonUIModel)
        }

        notifyItemInserted(blacklist.size - 1)
    }

    fun updateHeaderTextIfNeeded(newListLength: Int) {
        val header = blacklist
            .firstOrNull()
            ?.let { it as? BlacklistHeaderUIModel }

        if (header != null && header.listLength != newListLength) {
            header.listLength = newListLength
            blacklist[0] = header
            notifyItemChanged(0)
        }
    }

    fun removeItem(userId: Long) {
        try {
            val unblockedMember = blacklist
                .filterIsInstance<BlacklistedMemberUIModel>()
                .find { it.memberId == userId }

            if (unblockedMember != null) {
                val unblockedMemberIndex = blacklist.indexOf(unblockedMember)
                blacklist.removeAt(unblockedMemberIndex)
                notifyItemRangeRemoved(unblockedMemberIndex, 1)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun clearItemList() {
        val header = blacklist.firstOrNull()
        blacklist.clear()
        header?.also { blacklist.add(it) }
        notifyDataSetChanged()
    }
}
