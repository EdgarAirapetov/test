package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PurchaseModel(
    @SerializedName("account_color") val accountColor: Int,
    @SerializedName("account_type") val accountType: Int,
    @SerializedName("added_at") val addedAt: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("expires") val expires: Int,
    @SerializedName("gift_id") val giftId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("image") val image: String,
    @SerializedName("is_mine") val isMine: Int,
    @SerializedName("seen") val seen: Int,
    @SerializedName("small_image") val smallImage: String,
    @SerializedName("to_user_id") val toUserId: Int,
    @SerializedName("type_code") val typeCode: String,
    @SerializedName("user_id") val userId: Int
): Serializable