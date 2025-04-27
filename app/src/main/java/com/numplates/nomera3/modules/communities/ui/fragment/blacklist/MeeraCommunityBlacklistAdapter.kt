package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R
import timber.log.Timber
import kotlin.properties.Delegates

private const val LAST_ITEM_MARGIN_BOTTOM = 112

class MeeraCommunityBlacklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val RESERVED_LIST_SIZE = 2
    }

    val blacklist: MutableList<CommunityBlacklistUIModel> by Delegates.observable(
        mutableListOf(CommunityBlacklistUIModel.BlacklistHeaderUIModel(0))
    ) { _, _, newValue ->
        onDataSetChanged?.invoke(newValue.size)
    }

    var onDataSetChanged: ((Int) -> Unit)? = null
    var blacklistItemClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)? = null
    var blacklistContextMenuClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)? = null

    override fun getItemCount() = blacklist.size

    override fun getItemViewType(position: Int): Int {
        return if(blacklist[position] is CommunityBlacklistUIModel.BlacklistedMemberUIModel){
            R.layout.meera_blacklist_community_member_item
        } else {
            0
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == R.layout.meera_blacklist_community_member_item) {
            MeeraCommunityBlacklistMemberItemViewHolder(parent.inflate(R.layout.meera_blacklist_community_member_item))
        } else {
            CommunityBlacklistEmptyItemViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        addBottomMarginToLastItem(holder, position)
        val blacklistItem = blacklist[position]
        if (holder is MeeraCommunityBlacklistMemberItemViewHolder) {
            holder.bind(
                model = blacklistItem as CommunityBlacklistUIModel.BlacklistedMemberUIModel,
                itemClickListener = blacklistItemClickListener,
                contextMenuIconClickListener = blacklistContextMenuClickListener,
                isLastItem =  blacklist.size - 1 == position
            )
        }
    }

    private fun addBottomMarginToLastItem(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == blacklist.size - 1) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = LAST_ITEM_MARGIN_BOTTOM.dp
            holder.itemView.setLayoutParams(params)
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 0
            holder.itemView.setLayoutParams(params)
        }
    }

    fun addItemList(newItems: List<CommunityBlacklistUIModel>) {
        if (blacklist.last() is CommunityBlacklistUIModel.BlacklistClearButtonUIModel) {
            val clearButton = blacklist.removeAt(blacklist.lastIndex)
            blacklist.addAll(newItems)
            blacklist.add(clearButton)
        } else {
            blacklist.addAll(newItems)
            blacklist.add(CommunityBlacklistUIModel.BlacklistClearButtonUIModel)
        }
        notifyItemInserted(blacklist.size - 1)
    }

    fun updateHeaderTextIfNeeded(newListLength: Int) {
        val header = blacklist
            .firstOrNull()
            ?.let { it as? CommunityBlacklistUIModel.BlacklistHeaderUIModel }

        if (header != null && header.listLength != newListLength) {
            header.listLength = newListLength
            blacklist[0] = header
            notifyItemChanged(0)
        }
    }

    fun removeItem(userId: Long) {
        try {
            val unblockedMember = blacklist
                .filterIsInstance<CommunityBlacklistUIModel.BlacklistedMemberUIModel>()
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
