package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("apple_product_id")
    val appleProductId: String?,
    @SerializedName("custom_title")
    val customTitle: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("image")
    val imageItem: ImageItemDto,
    @SerializedName("itunes_product_id")
    val itunesProductId: String?,
    @SerializedName("play_market_product_id")
    val playMarketProductId: String?,
    @SerializedName("type")
    val type: Long?,
    @SerializedName("price")
    var price: String? = ""
)

data class ImageItemDto(
    @SerializedName("link")
    val link: String?,
    @SerializedName("link_small")
    val linkSmall: String?
)
