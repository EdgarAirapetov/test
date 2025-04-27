package com.numplates.nomera3.presentation.model
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class MutualFriendsUiEntity(
    val mutualFriends: List<MutualUser>,
    val moreCount: Int,
    val accountTypeEnum: AccountTypeEnum
)

data class MutualUser(
    val id: Long,
    val name: String,
    val avatarSmall: String
)
