package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityBannerFloor (
    val bannerType: BannerType,
    val userType: AccountTypeEnum,
    override var isSeparable: Boolean = true
): UserUIEntity, Separable {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.BANNER_FLOOR
}

enum class BannerType(var value: Int) {
    BANNER_TYPE_GIFT(0),
    BANNER_TYPE_BLOCKED_ME(1)
}
