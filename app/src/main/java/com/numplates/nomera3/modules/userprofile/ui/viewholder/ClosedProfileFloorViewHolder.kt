package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityClosedProfileFloor

class ClosedProfileFloorViewHolder(val parent: ViewGroup) :
    BaseUserViewHolder<UserEntityClosedProfileFloor>(parent, R.layout.item_closed_profile_floor) {
    override fun bind(data: UserEntityClosedProfileFloor) = Unit
}
