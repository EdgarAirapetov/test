package com.numplates.nomera3.modules.userprofile.ui.viewholder

import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraClosedProfileFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData

class MeeraBlockedMeProfileFloorViewHolder(
    val binding: MeeraClosedProfileFloorItemBinding,
): BaseVH<UserInfoRecyclerData, MeeraClosedProfileFloorItemBinding>(binding) {
    override fun bind(data: UserInfoRecyclerData) {
        binding.closedProfileLabel.text = binding.root.resources.getString(
            R.string.meera_profile_is_blocked_me
        )
    }
}
