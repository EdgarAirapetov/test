package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

data class UserEntityMapFloor(
    val accountTypeEnum: AccountTypeEnum,
    val accountColor: Int? = 0,
    val userAvatarSmall: String? = null,
    val isMe: Boolean = true,
    val distance: Int? = null,
    val value: Int? = null,
    val countBlacklist: Int? = null,
    val countWhitelist: Int? = null,
    val coordinates: CoordinatesUIModel? = null,
    override var isSeparable: Boolean = true
): UserUIEntity, Separable {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.MAP_FLOOR
}

data class CoordinatesUIModel(
    val latitude: Double?,
    val longitude: Double?
)
