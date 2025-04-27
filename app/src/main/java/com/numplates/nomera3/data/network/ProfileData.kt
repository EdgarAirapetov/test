package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class ProfileData (
    @SerializedName("account_type") var accountType: Int,
    @SerializedName("account_color") var accountColor: Int,
    @SerializedName("purchase_expiration") var purchaseExpiration: Int
): Serializable
