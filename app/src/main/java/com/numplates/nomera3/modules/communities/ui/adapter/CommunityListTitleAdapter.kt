package com.numplates.nomera3.modules.communities.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.communities.ui.fragment.list.CommunityListUIModel

class CommunityListTitleAdapter(
    private val title: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommunityListTitleViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CommunityListTitleViewHolder)
            .bind(CommunityListUIModel.CommunityListTitle(title))
    }

    override fun getItemCount() = 1

}