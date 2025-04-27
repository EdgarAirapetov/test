package com.numplates.nomera3.modules.purchase.ui.mapper

import com.numplates.nomera3.modules.purchase.domain.model.GiftCategoryModel
import com.numplates.nomera3.modules.purchase.domain.model.GiftItemModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import javax.inject.Inject

class GiftUiModelMapper @Inject constructor() {

    fun convertGiftCategory(giftCategories: List<GiftCategoryModel>): List<GiftCategoryUiModel> {
        return giftCategories.map { category ->
            GiftCategoryUiModel(
                categoryName = category.categoryName,
                gifts = category.gifts.map(this::convertGiftItem),
            )
        }
    }

    fun convertGiftItem(giftItem: GiftItemModel): GiftItemUiModel {
        return GiftItemUiModel(
            giftId = giftItem.giftId,
            marketProductId = giftItem.marketProductId,
            smallImage = giftItem.smallImage,
            image = giftItem.image,
            customTitle = giftItem.customTitle,
            type = giftItem.type,
            customDesc = giftItem.customDesc,
            price = giftItem.price,
        )
    }
}
