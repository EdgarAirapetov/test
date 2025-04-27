package com.numplates.nomera3.modules.userprofile.domain.model.usermain

import com.meera.db.models.userprofile.ProductEntity

data class ProductHolidayModel(
    val id: Long?,
    val appleProductId: String?,
    val customTitle: String?,
    val description: String?,
    val imageItem: ProductEntity.ImageItemEntity,
    val itunesProductId: String?,
    val playMarketProductId: String?,
    val type: Long?,
    var price: String? = "",
    val imageLink: String?,
    val imageLinkSmall: String?
)
