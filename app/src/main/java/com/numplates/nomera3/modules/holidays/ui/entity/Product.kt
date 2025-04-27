package com.numplates.nomera3.modules.holidays.ui.entity

data class Product(
    val id: Long?,
    val appleProductId: String?,
    val customTitle: String?,
    val description: String?,
    val imageItem: ImageItem,
    val itunesProductId: String?,
    val playMarketProductId: String?,
    val type: Long?,
) {

    data class ImageItem(
        val link: String?,
        val linkSmall: String?
    )

}