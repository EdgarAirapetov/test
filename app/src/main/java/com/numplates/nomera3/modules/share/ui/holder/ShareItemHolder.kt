package com.numplates.nomera3.modules.share.ui.holder

import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.BaseViewHolder
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.numplates.nomera3.presentation.view.widgets.VipView

class ShareItemHolder(
    val parent: ViewGroup,
    private val callback: ShareItemsCallback
) : BaseViewHolder(parent, R.layout.item_share) {

    private val vvFriendAvatar: VipView = itemView.findViewById(R.id.vipView_friend_holder)
    private val tvShareItemName: TextView = itemView.findViewById(R.id.tv_share_friend_name)
    private val uniqueNameTextView: TextView = itemView.findViewById(R.id.uniqueNameTextView)
    private val cvGroupBadge: CardView = itemView.findViewById(R.id.cv_icon_badge)
    private val cbShareItem: CheckBox = itemView.findViewById(R.id.cb_share_friend)

    fun bind(item: UIShareItem) {
        setupCheckBox(item)
        setupTitle(item)
        setupSubtitle(item)
        setupAvatar(item)
        setupGroupBadge(item)
    }

    private fun setupGroupBadge(item: UIShareItem) {
        if (item.isGroupChat) cvGroupBadge.visible()
        else cvGroupBadge.gone()
    }

    private fun setupAvatar(item: UIShareItem) {
        vvFriendAvatar.setUp(
            itemView.context,
            item.avatar,
            item.accountTypeEnum.value,
            item.color,
            true
        )
    }

    private fun setupSubtitle(item: UIShareItem) {
        uniqueNameTextView.text = item.subTitle
    }

    private fun setupTitle(item: UIShareItem) {
        tvShareItemName.text = item.title
        val isApproved = item.approved == 1
        val isVip = item.accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP
        tvShareItemName.enableApprovedIcon(isApproved, isVip)
    }

    private fun setupCheckBox(item: UIShareItem) {
        cbShareItem.setOnClickListener {
            if (callback.canBeChecked() || item.isChecked) {
                callback.onChecked(item, !item.isChecked)
            } else {
                cbShareItem.isChecked = item.isChecked
            }
        }
        cbShareItem.isChecked = item.isChecked
    }
}
