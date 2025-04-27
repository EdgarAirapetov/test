package com.numplates.nomera3.modules.bump.ui.entity

import com.numplates.nomera3.presentation.model.MutualUsersUiModel

data class UserShakeUiModel(
    val userId: Long,
    val name: String,
    val avatarSmall: String,
    val mutualUsers: MutualUsersUiModel?,
    val userFriendShakeStatus: UserFriendShakeStatus,
    val labelText: String,
    val approvedUser: Boolean,
    val topContentMaker: Boolean
)
