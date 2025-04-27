package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.Separable
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

class UserEntityGiftsFloor(
    val listGiftEntity: List<GiftUIModel>,
    val giftsCount: Int,
    val giftsNewCount: Int,
    val accountTypeEnum: AccountTypeEnum,
    val isMe: Boolean = true,
    override var isSeparable: Boolean = true
) : UserUIEntity, Separable {

    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.GIFTS_FLOOR

}

data class GiftUIModel(
    val giftId: Long,
    val image: String,
    val typeId: Int,
    val isViewed: Boolean,
    val isReceived: Boolean
)
