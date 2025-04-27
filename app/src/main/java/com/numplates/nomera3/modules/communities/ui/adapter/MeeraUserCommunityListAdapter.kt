package com.numplates.nomera3.modules.communities.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemCommunitiesListTitleBinding
import com.numplates.nomera3.databinding.MeeraUserCommunityListItemBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel
import com.numplates.nomera3.presentation.view.utils.inflateBinding

private const val ONE_ELEMENT = 1

class MeeraUserCommunityListAdapter : ListAdapter<CommunityListUIModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    val isEmptyOrLastItemTitle: Boolean
        get() = currentList.size == ONE_ELEMENT
            && currentList.first() is CommunityListUIModel.CommunityListTitle

    var itemClickListener: ((CommunityListItemUIModel?) -> Unit)? = null
    var subscriptionClickListener: ((community: CommunityListItemUIModel, position: Int) -> Unit)? = null
    private var removedCommunityListItem: Pair<Int, CommunityListItemUIModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_communities_list_title ->
                MeeraCommunityListTitleViewHolder(
                    parent.inflateBinding(
                        ItemCommunitiesListTitleBinding::inflate
                    )
                )

            else -> MeeraCommunityViewHolder(
                parent.inflateBinding(
                    MeeraUserCommunityListItemBinding::inflate
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        when (holder) {
            is MeeraCommunityListTitleViewHolder -> holder.bind(item as CommunityListUIModel.CommunityListTitle)
            else -> holder
                .let { it as? MeeraCommunityViewHolder }
                ?.bind(
                    community = item as? CommunityListUIModel.Community,
                    lastPosition = currentList.lastIndex == position,
                    itemClickListener = itemClickListener,
                    subscriptionClickListener = subscriptionClickListener
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is CommunityListUIModel.CommunityListTitle -> R.layout.item_communities_list_title
            is CommunityListUIModel.Community -> R.layout.meera_user_community_list_item
        }
    }

    fun restoreLastRemovedItem(context: Context) {
        val index = removedCommunityListItem?.first
        val community = removedCommunityListItem?.second
        if (index != null && community != null) {
            restoreTitleIfRemoved(context)
            removedCommunityListItem = null
            currentList.add(index, CommunityListUIModel.Community(community))
            notifyItemRangeInserted(index, ONE_ELEMENT)
            setListQuantityTitle(context)
        }
    }

    private fun restoreTitleIfRemoved(context: Context) {
        if (currentList.isEmpty()) {
            currentList.add(
                CommunityListUIModel.CommunityListTitle(
                    context.pluralString(R.plurals.communities_plural, ONE_ELEMENT)
                )
            )
            notifyItemInserted(0)
        }
    }

    fun removeItem(communityId: Long, context: Context) {
        val community = currentList.find {
            (it as? CommunityListUIModel.Community)?.community?.id?.toLong() == communityId
        }
        if (community != null) {
            val index = currentList
                .indexOf(community)
                .takeIf { it != -1 }

            if (index != null) {
                removedCommunityListItem =
                    index to (community as CommunityListUIModel.Community).community
                currentList.removeAt(index)
                notifyItemRangeRemoved(index, ONE_ELEMENT)
            }
        }
        if (currentList.size > ONE_ELEMENT) setListQuantityTitle(context)
    }

    private fun setListQuantityTitle(context: Context) {
        if (currentList.isEmpty()) return
        currentList.first().let {
            (it as? CommunityListUIModel.CommunityListTitle)?.title =
                context.pluralString(R.plurals.communities_plural, currentList.size - ONE_ELEMENT)
            notifyItemChanged(0)
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommunityListUIModel>() {
    override fun areContentsTheSame(oldItem: CommunityListUIModel, newItem: CommunityListUIModel): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: CommunityListUIModel, newItem: CommunityListUIModel): Boolean {
        return oldItem == newItem
    }
}
