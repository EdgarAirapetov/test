package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.fcm.data.User
import java.io.Serializable

data class Comment(
    @SerializedName("id") override var id: Long,
    @SerializedName("uid") override var uid: Long,
    @SerializedName("purchases") var purchases: Int,
    @SerializedName("vehicle") override var vehicle: Int,
    @SerializedName("name") override var name: String?,
    @SerializedName("number") override var number: String?,
    @SerializedName("avatar") override var avatar: String?,
    @SerializedName("avatar_date") override var avatarDate: Long,
    @SerializedName("driver") override var driver: Int,
    @SerializedName("text") override var text: String?,
    @SerializedName("date") override var date: Long,
    @SerializedName("cid") var cid: Int,
    @SerializedName("resp_name") override var respName: String?,
    @SerializedName("account_color") override var accountColor: Int,
    @SerializedName("account_type") override var accountType: Int,
    @SerializedName("purchase_basket") var purchaseBasket: List<Purchase?>?,
    @SerializedName("user") var user: User
) : SimplePost(), Serializable