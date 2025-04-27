package com.numplates.nomera3.modules.communities.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemCommunitiesListTitleBinding
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel

class MeeraCommunityListTitleViewHolder(val binding: ItemCommunitiesListTitleBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CommunityListUIModel.CommunityListTitle?) {
        binding.tvTitle.text = item?.title
    }
}
