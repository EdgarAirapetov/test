package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityHolidayFloor(
  val isVip: Boolean,
  val giftItem: GiftItemDto,
  override var isSeparable: Boolean = true
): UserUIEntity, Separable {

    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.HOLIDAY_FLOOR
}
