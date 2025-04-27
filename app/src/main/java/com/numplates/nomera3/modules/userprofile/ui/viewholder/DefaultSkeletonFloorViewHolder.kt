package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityDefaultSkeletonFloor

class DefaultSkeletonFloorViewHolder(
    val parent: ViewGroup
) : BaseUserViewHolder<UserEntityDefaultSkeletonFloor>(parent, R.layout.item_skeleton_floor) {
    override fun bind(data: UserEntityDefaultSkeletonFloor) = Unit
}
