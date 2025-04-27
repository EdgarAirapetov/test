package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.ViewGroup
import android.widget.TextView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel

class CommunityListTitleViewHolder(viewGroup: ViewGroup) :
    BaseViewHolder(viewGroup, R.layout.item_communities_list_title) {

    private val tvTitle: TextView? = itemView.findViewById(R.id.tv_title)

    fun bind(item: CommunityListUIModel.CommunityListTitle?) {
        tvTitle?.text = item?.title
    }
}