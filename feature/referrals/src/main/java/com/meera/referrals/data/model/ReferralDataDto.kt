package com.meera.referrals.data.model

import com.google.gson.annotations.SerializedName

data class ReferralDataDto(

    @SerializedName("available_vips")
    val availableVips: Int,

    @SerializedName("code")
    val code: String,

    @SerializedName("referals")
    val referrals: ReferralDto,

    @SerializedName("text")
    val text: String,

    @SerializedName("title")
    val title: String
)



data class ReferralDto(

    @SerializedName("count")
    val count: Int,

    @SerializedName("limit")
    val limit: Int
)
