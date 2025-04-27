package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraBirthdayFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraBirthdayFloorViewHolder(
    val binding: MeeraBirthdayFloorItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraBirthdayFloorItemBinding>(binding) {

    override fun bind(data: UserInfoRecyclerData) {
        binding.happyBirthdayBtn.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.StartChatClick)
        }

        binding.ivCloseBtn.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.OnCloseCongratulationClick)
        }
    }
}
