package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductEntity(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("apple_product_id")
    val appleProductId: String?,
    @SerializedName("custom_title")
    val customTitle: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("image")
    val imageItem: ImageItemEntity,
    @SerializedName("itunes_product_id")
    val itunesProductId: String?,
    @SerializedName("play_market_product_id")
    val playMarketProductId: String?,
    @SerializedName("type")
    val type: Long?,
    var price: String = ""
): Parcelable {

    @Parcelize
    data class ImageItemEntity(
        @SerializedName("link")
        val link: String?,
        @SerializedName("link_small")
        val linkSmall: String?
    ): Parcelable
}
