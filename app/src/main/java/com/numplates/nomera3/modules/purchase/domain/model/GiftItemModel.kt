package com.numplates.nomera3.modules.purchase.domain.model

class GiftItemModel(
    val giftId: Long,
    val marketProductId: String,
    val smallImage: String,
    val image: String?,
    val customTitle: String,
    val type: Int,
    val customDesc: String? = null,
    val price: String?,
)
