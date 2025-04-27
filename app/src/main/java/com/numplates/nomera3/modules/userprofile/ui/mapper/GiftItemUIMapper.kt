package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import javax.inject.Inject

class GiftItemUIMapper @Inject constructor() {
    fun mapToGiftUIModel(gift: GiftItemDto) = GiftItemUiModel(
        giftId = gift.giftId,
        marketProductId = gift.marketProductId,
        smallImage = gift.smallImage,
        image = gift.image,
        customDesc = gift.customDesc,
        customTitle = gift.customTitle,
        type = gift.type,
        price = gift.price,
    )
}
