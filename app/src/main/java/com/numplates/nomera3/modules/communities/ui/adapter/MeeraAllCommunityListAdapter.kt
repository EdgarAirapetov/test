package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemCommunitiesListTitleBinding
import com.numplates.nomera3.databinding.MeeraUserCommunityListItemBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraAllCommunityListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemClickListener: ((CommunityListItemUIModel?) -> Unit)? = null
    var subscriptionClickListener: ((community: CommunityListItemUIModel, position: Int) -> Unit)? = null
    private val communityList: MutableList<CommunityListUIModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_communities_list_title ->
                MeeraCommunityListTitleViewHolder(parent.inflateBinding(
                    ItemCommunitiesListTitleBinding::inflate
                ))
            else -> MeeraCommunityViewHolder(parent.inflateBinding(
                MeeraUserCommunityListItemBinding::inflate
            ))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = communityList[position]
        when (holder) {
            is MeeraCommunityListTitleViewHolder -> holder.bind(item as CommunityListUIModel.CommunityListTitle)
            else -> holder
                .let { it as? MeeraCommunityViewHolder }
                ?.bind(
                    community = item as? CommunityListUIModel.Community,
                    lastPosition = communityList.lastIndex == position,
                    itemClickListener = itemClickListener,
                    subscriptionClickListener = subscriptionClickListener
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (communityList[position]) {
            is CommunityListUIModel.CommunityListTitle -> R.layout.item_communities_list_title
            is CommunityListUIModel.Community -> R.layout.meera_user_community_list_item
        }
    }

    override fun getItemCount() = communityList.size

    fun addItemList(newUsers: List<CommunityListUIModel>) {
        communityList.addAll(newUsers)
        notifyItemInserted(communityList.size - 1)
    }

    fun replace(items: List<CommunityListUIModel>?) {
        items?.let {
            communityList.clear()
            communityList.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun clearItemList() {
        communityList.clear()
        notifyDataSetChanged()
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
