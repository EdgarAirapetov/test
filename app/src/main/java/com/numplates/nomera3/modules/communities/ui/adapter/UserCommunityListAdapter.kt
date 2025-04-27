package com.numplates.nomera3.modules.communities.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel
import com.meera.core.extensions.pluralString

class UserCommunityListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val isEmptyOrLastItemTitle: Boolean
        get() = communityList.size == 1
                    && communityList.first() is CommunityListUIModel.CommunityListTitle

    var onEmptyListListener: (() -> Unit)? = null
    var itemClickListener: ((CommunityListItemUIModel?) -> Unit)? = null
    var subscriptionClickListener: ((community: CommunityListItemUIModel, position: Int) -> Unit)? = null
    private val communityList: MutableList<CommunityListUIModel> = mutableListOf()
    private var removedCommunityListItem: Pair<Int, CommunityListItemUIModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.item_communities_list_title -> CommunityListTitleViewHolder(parent)
            else -> CommunityViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = communityList[position]
        when (holder) {
            is CommunityListTitleViewHolder -> holder.bind(item as CommunityListUIModel.CommunityListTitle)
            else -> holder
                .let { it as? CommunityViewHolder }
                ?.bind(
                    item as? CommunityListUIModel.Community, itemClickListener,
                    subscriptionClickListener
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (communityList[position]) {
            is CommunityListUIModel.CommunityListTitle -> R.layout.item_communities_list_title
            is CommunityListUIModel.Community -> R.layout.user_community_list_item
        }
    }

    override fun getItemCount() = communityList.size

    fun addItemList(newUsers: List<CommunityListUIModel>) {
        communityList.addAll(newUsers)
        notifyItemInserted(communityList.size - 1)
    }

    fun clearItemList() {
        communityList.clear()
        notifyDataSetChanged()
    }

    fun restoreLastRemovedItem(context: Context) {
        val index = removedCommunityListItem?.first
        val community = removedCommunityListItem?.second
        if (index != null && community != null) {
            restoreTitleIfRemoved(context)
            removedCommunityListItem = null
            communityList.add(index, CommunityListUIModel.Community(community))
            notifyItemRangeInserted(index, 1)
            setListQuantityTitle(context)
        }
    }

    private fun restoreTitleIfRemoved(context: Context) {
        if (communityList.isEmpty()) {
            communityList.add(CommunityListUIModel.CommunityListTitle(
                context.pluralString(R.plurals.communities_plural, 1)
            ))
            notifyItemInserted(0)
        }
    }

    fun removeItem(communityId: Long, context: Context) {
        val community = communityList.find {
            (it as? CommunityListUIModel.Community)?.community?.id?.toLong() == communityId
        }
        if (community != null) {
            val index = communityList
                .indexOf(community)
                .takeIf { it != -1 }

            if (index != null) {
                removedCommunityListItem =
                    index to (community as CommunityListUIModel.Community).community
                communityList.removeAt(index)
                notifyItemRangeRemoved(index, 1)
            }
        }
        if (communityList.size > 1) setListQuantityTitle(context)
    }

    private fun setListQuantityTitle(context: Context) {
        if (communityList.isEmpty()) return
        communityList.first().let {
            (it as? CommunityListUIModel.CommunityListTitle)?.title =
                context.pluralString(R.plurals.communities_plural, communityList.size - 1)
            notifyItemChanged(0)
        }
    }

    fun setSubscribed(position: Int, subscribed: Boolean) {
        if (communityList.isEmpty()) return
        communityList[position].let {
            val item = (it as? CommunityListUIModel.Community)
            item?.community?.isMember = subscribed
            if (!subscribed) item?.community?.userStatus = CommunityEntity.USER_STATUS_NOT_SENT
        }
        notifyItemChanged(position)
    }

    fun getIndexByGroupId(groupId: Int): Int? {
        val item = communityList.find {
            (it as? CommunityListUIModel.Community)?.community?.id == groupId
        }
        return if (item != null) communityList.indexOf(item)
        else null
    }
}