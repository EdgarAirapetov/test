package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class SubscribersFloorUiEntity (
    val subscribersCount: Long = 0,
    val subscriptionCount: Long = 0,
    val friendsCount: Long = 0,
    val mutualFriendsAndSubscribersCount: Int = 0,
    val friendsRequestCount: Long = 0,
    val showFriendsSubscribers: Boolean = false,
    val isMe: Boolean = true,
    val userStatus: AccountTypeEnum
): UserUIEntity {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.SUBSCRIBERS_FLOOR
}

