package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.presentation.model.IImageData
import com.numplates.nomera3.presentation.view.view.GiftListItem
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class Purchase(
    @SerializedName("id") var id: Long,
    @SerializedName("is_mine") var isMine: Int,
    @SerializedName("gift_id") var giftId: Int,
    @SerializedName("small_image") var smallImage: String?,
    @SerializedName("image") var image: String?,
    @SerializedName("type_code") var typeCode: String?,
    @SerializedName("added_at") var addedAt: Int,
    @SerializedName("purchase_date") var purchaseDate: Int
) : Serializable, IImageData, ProfileListItem, GiftListItem {

    override val caption: String?
        get() = null
    override val imageUrl: String?
        get() = image
    override val num: String?
        get() = null

    override val giftUserId: Long
        get() = id
    override val giftIsMine: Int
        get() = isMine
    override val giftGetId: Int
        get() = giftId
    override val giftSmallImage: String?
        get() = smallImage
    override val giftImage: String?
        get() = image
    override val giftTypeCode: String?
        get() = typeCode
    override val giftAddedAt: Int
        get() = addedAt
    override val giftPurchaseDate: Int
        get() = purchaseDate
}