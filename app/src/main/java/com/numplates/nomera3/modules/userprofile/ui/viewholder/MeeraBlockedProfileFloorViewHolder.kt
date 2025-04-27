package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraBlockedProfileFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraBlockedProfileFloorViewHolder(
    val binding: MeeraBlockedProfileFloorItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
): BaseVH<UserInfoRecyclerData, MeeraBlockedProfileFloorItemBinding>(binding) {
    override fun bind(data: UserInfoRecyclerData) {
        binding.vUnblockBtn.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnBlacklistUserClickedAction)
        }
    }
}
