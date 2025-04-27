package com.numplates.nomera3.modules.purchase.data.model

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.model.IImageData
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class GiftItemDto(
    @SerializedName("gift_id") val giftId: Long,
    @SerializedName("apple_product_id") val appleProductId: String,
    @SerializedName("itunes_product_id") val itunesProductId: String,
    @SerializedName("play_market_product_id") val marketProductId: String,
    @SerializedName("small_image") val smallImage: String,
    @SerializedName("image") val image: String?,
    @SerializedName("custom_title") val customTitle: String,
    @SerializedName("type") val type: Int,
    @SerializedName("description") val customDesc: String? = null,

    @Deprecated("It's incorrect to have extra fields in DTO objects.")
    val price: String? = "",
) : Serializable, IImageData, ProfileListItem {

    override val caption: String
        get() = customTitle

    override val imageUrl: String?
        get() = image

    override val num: String?
        get() = price
}
