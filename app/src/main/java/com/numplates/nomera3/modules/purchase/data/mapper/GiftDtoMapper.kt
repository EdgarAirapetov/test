package com.numplates.nomera3.modules.purchase.data.mapper

import com.android.billingclient.api.ProductDetails
import com.numplates.nomera3.modules.purchase.data.model.GiftCategoryDto
import com.numplates.nomera3.modules.purchase.data.model.GiftItemDto
import javax.inject.Inject

class GiftDtoMapper @Inject constructor() {

    fun convertGiftItemData(
        giftCategories: List<GiftCategoryDto>,
        productDetails: List<ProductDetails>,
    ): List<GiftCategoryDto> {
        return giftCategories.map { category ->
            val gifts = category.gifts.map { giftItem ->
                val details = productDetails.find { it.productId == giftItem.marketProductId }
                giftItem.copy(price = details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
            }
            category.copy(gifts = gifts)
        }
    }

    fun flatMapProductIds(giftItems: List<GiftItemDto>): List<String> {
        return giftItems.map { item -> item.marketProductId }
    }
}
