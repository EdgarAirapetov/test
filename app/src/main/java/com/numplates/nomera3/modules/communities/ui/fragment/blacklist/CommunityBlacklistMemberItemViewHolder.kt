package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.meera.core.extensions.click
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.BaseViewHolder

class CommunityBlacklistMemberItemViewHolder(
    viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.blacklist_community_member_item) {

    private var itemRootView: View? = itemView.findViewById(R.id.blacklistItemRootView)
    private var memberNameView: TextView? = itemView.findViewById(R.id.blacklistMemberName)
    private var memberPhotoView: ImageView? = itemView.findViewById(R.id.blacklistMemberPhoto)
    private var openContextMenuView: ImageView? = itemView.findViewById(R.id.blacklistContextMenu)
    private var bufferedBlacklistedMemberUIModel: CommunityBlacklistUIModel.BlacklistedMemberUIModel? = null

    fun bind(
        model: CommunityBlacklistUIModel.BlacklistedMemberUIModel,
        itemClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)?,
        contextMenuIconClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)?
    ) {
        bufferedBlacklistedMemberUIModel = model
        memberPhotoView?.loadGlideCircleWithPlaceHolder(model.memberPhotoUrl, R.drawable.fill_8_round)
        memberNameView?.text = model.memberName

        itemRootView?.click { itemClickListener?.invoke(bufferedBlacklistedMemberUIModel) }
        openContextMenuView?.click { contextMenuIconClickListener?.invoke(bufferedBlacklistedMemberUIModel) }
    }
}
