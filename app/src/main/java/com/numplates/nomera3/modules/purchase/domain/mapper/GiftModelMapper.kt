package com.numplates.nomera3.modules.purchase.domain.mapper

import com.android.billingclient.api.ProductDetails
import com.numplates.nomera3.modules.purchase.data.model.GiftCategoryDto
import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import com.numplates.nomera3.modules.purchase.domain.model.GiftCategoryModel
import com.numplates.nomera3.modules.purchase.domain.model.GiftItemModel
import javax.inject.Inject

class GiftModelMapper @Inject constructor() {

    fun convertGiftCategory(
        giftCategories: List<GiftCategoryDto>,
        productDetails: List<ProductDetails>,
    ): List<GiftCategoryModel> {
        return giftCategories.map { category ->
            val gifts = category.gifts.map { giftItem ->
                convertGiftItem(
                    giftItem = giftItem,
                    price = productDetails.find { it.productId == giftItem.marketProductId }
                        ?.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                )
            }
            GiftCategoryModel(
                categoryName = category.categoryName,
                gifts = gifts,
            )
        }
    }

    fun convertGiftItem(giftItem: GiftItemDto, price: String? = null): GiftItemModel {
        return GiftItemModel(
            giftId = giftItem.giftId,
            marketProductId = giftItem.marketProductId,
            smallImage = giftItem.smallImage,
            image = giftItem.image,
            customTitle = giftItem.customTitle,
            type = giftItem.type,
            customDesc = giftItem.customDesc,
            price = price,
        )
    }
}
