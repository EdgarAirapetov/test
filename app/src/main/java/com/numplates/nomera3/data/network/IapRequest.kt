package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IapRequest(
    @SerializedName("orderId") var orderId: String,
    @SerializedName("packageName") var packageName: String,
    @SerializedName("productId") var productId: String,
    @SerializedName("purchaseTime") var purchaseTime: Long,
    @SerializedName("purchaseState") var purchaseState: Int,
    @SerializedName("purchaseToken") var purchaseToken: String,
    @SerializedName("userId") var userId: Long?,
    @SerializedName("comment") var comment: String?,
    @SerializedName("colorId") var accountColor: Int?,
    @SerializedName("show_sender") var showSender: Boolean? = null
) : Serializable
