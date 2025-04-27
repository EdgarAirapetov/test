package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.ViewGroup
import android.widget.TextView
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.ZERO_BLOCKED_COMMUNITY_MEMBERS

class CommunityBlacklistLengthItemViewHolder(
    viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.blacklist_community_length_item) {

    private var blacklistLengthTextView: TextView? = itemView.findViewById(R.id.blacklistLengthText)

    fun bind(model: CommunityBlacklistUIModel.BlacklistHeaderUIModel?) {
        if (model != null) {
            blacklistLengthTextView?.text = itemView.context
                ?.pluralString(R.plurals.community_blacklist_plurals, model.listLength)
                ?: ZERO_BLOCKED_COMMUNITY_MEMBERS
        }
    }
}
