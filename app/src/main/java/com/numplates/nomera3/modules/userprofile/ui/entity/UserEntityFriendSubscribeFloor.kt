package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityFriendSubscribeFloor(
    val isUserBlacklisted: Boolean,
    val userId: Long,
    val friendStatus: Int,
    val isSubscribed: Boolean,
    val userStatus: AccountTypeEnum,
    val approved: Boolean,
    val topContentMaker: Boolean,
    val name: String
) : UserUIEntity {

    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.FRIEND_SUBSCRIBE_FLOOR
}
