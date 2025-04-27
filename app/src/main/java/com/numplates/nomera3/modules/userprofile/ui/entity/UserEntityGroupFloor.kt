package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityGroupFloor(
    val groups: List<GroupUIModel>,
    val groupCount: Int,
    val userTypeEnum: AccountTypeEnum
): UserUIEntity {

    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.GROUPS_FLOOR

}

data class GroupUIModel(
    val id: Long,
    val name: String,
    val avatar: String,
    val countMembers: Int
)
