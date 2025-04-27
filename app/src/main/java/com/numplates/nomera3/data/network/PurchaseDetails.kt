package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class PurchaseDetails (
    @SerializedName("purchase_data") var purchaseData: PurchaseData,
    @SerializedName("profile") var profile: ProfileData
): Serializable
