package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraUpdateAppItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraUpdateAppViewHolder(
    val binding: MeeraUpdateAppItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraUpdateAppItemBinding>(binding) {

    override fun bind(data: UserInfoRecyclerData) {
        binding.cvUpdateBtnRoot.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.UpdateButtonClicked)
        }
    }
}
