package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityRoadFloor(
    val postCount: Int,
    val userTypeEnum: AccountTypeEnum,
    val isMe: Boolean = true
): UserUIEntity {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.ROAD_FLOOR
}