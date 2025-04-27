package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.ViewGroup
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.MutualSubscribersUiEntity
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import com.numplates.nomera3.presentation.view.widgets.MutualFriendsView

class MutualSubscribersHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<MutualSubscribersUiEntity>(parent, R.layout.item_mutual_subscribers) {

    private val vgMutualFriends: MutualFriendsView = itemView.findViewById(R.id.vg_mutual_friends)

    override fun bind(data: MutualSubscribersUiEntity) {
        setUserData(data)
        initListeners()
    }

    private fun initListeners() {
        itemView.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnMutualFriendsClicked)
        }
    }

    private fun setUserData(data: MutualSubscribersUiEntity) {
        vgMutualFriends.setMutualFriends(
            MutualFriendsUiEntity(
                moreCount = data.moreCount,
                mutualFriends = data.mutualSubscribersFriends,
                accountTypeEnum = data.userType
            )
        )
        val typeColor = getTextColorByAccountType(data.userType)
        vgMutualFriends.setTextColor(typeColor)
    }

    private fun getTextColorByAccountType(accountTypeEnum: AccountTypeEnum): Int {
        return if (accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP) {
            R.color.ui_white
        } else {
            R.color.colorBlack
        }
    }
}
