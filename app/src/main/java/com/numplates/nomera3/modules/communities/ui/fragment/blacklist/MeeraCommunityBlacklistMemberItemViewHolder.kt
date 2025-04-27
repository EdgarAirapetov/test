package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R

class MeeraCommunityBlacklistMemberItemViewHolder(
    view: View,
): RecyclerView.ViewHolder(view) {

    private val cell: UiKitCell = view.findViewById(R.id.group_blacklist_member_cell)
    private var bufferedBlacklistedMemberUIModel: CommunityBlacklistUIModel.BlacklistedMemberUIModel? = null

    fun bind(
        model: CommunityBlacklistUIModel.BlacklistedMemberUIModel,
        itemClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)?,
        contextMenuIconClickListener: ((CommunityBlacklistUIModel.BlacklistedMemberUIModel?) -> Unit)?,
        isLastItem: Boolean
    ) {
        bufferedBlacklistedMemberUIModel = model
        cell.setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = model.memberPhotoUrl))
        cell.setTitleValue(model.memberName)
        cell.setDescriptionValue("@${model.uniqueName}")
        cell.setRightIconClickListener { contextMenuIconClickListener?.invoke(bufferedBlacklistedMemberUIModel) }
        itemView.setThrottledClickListener { itemClickListener?.invoke(bufferedBlacklistedMemberUIModel) }
        if (isLastItem) cell.cellPosition = CellPosition.BOTTOM
    }

}
