package com.numplates.nomera3.modules.holidays.domain.entity

data class ProductModel(
    val id: Long?,
    val appleProductId: String?,
    val customTitle: String?,
    val description: String?,
    val imageItem: ImageItemModel,
    val itunesProductId: String?,
    val playMarketProductId: String?,
    val type: Long?,
) {

    data class ImageItemModel(
        val link: String?,
        val linkSmall: String?
    )

}
