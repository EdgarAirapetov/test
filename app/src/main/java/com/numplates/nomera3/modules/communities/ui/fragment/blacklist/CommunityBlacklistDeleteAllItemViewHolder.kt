package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder

class CommunityBlacklistDeleteAllItemViewHolder(
    viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.blacklist_community_delete_all_item) {

    fun bind(clearBlacklistClickListener: (() -> Unit)?) {
        itemView.setOnClickListener {
            clearBlacklistClickListener?.invoke()
        }
    }
}
