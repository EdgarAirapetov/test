package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMutualSubscribersItemBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity

class MeeraMutualSubscribersHolder(
    val binding: MeeraMutualSubscribersItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraMutualSubscribersItemBinding>(binding) {

    override fun bind(data: UserInfoRecyclerData) {
        data as UserInfoRecyclerData.MutualSubscribersUiEntity
        setUserData(data)
        initListeners()
    }

    private fun initListeners() {
        itemView.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnMutualFriendsClicked)
        }
    }

    private fun setUserData(data: UserInfoRecyclerData.MutualSubscribersUiEntity) {
        binding.vgMutualFriends.setMutualFriends(
            MutualFriendsUiEntity(
                moreCount = data.moreCount,
                mutualFriends = data.mutualSubscribersFriends,
                accountTypeEnum = data.userType
            )
        )
        val typeColor = getTextColorByAccountType(data.userType)
        binding.vgMutualFriends.setTextColor(typeColor)
    }

    private fun getTextColorByAccountType(accountTypeEnum: AccountTypeEnum): Int {
        return if (accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP) {
            R.color.ui_white
        } else {
            R.color.colorBlack
        }
    }
}
