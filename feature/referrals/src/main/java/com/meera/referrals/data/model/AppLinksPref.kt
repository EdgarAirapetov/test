package com.meera.referrals.data.model

import com.google.gson.annotations.SerializedName

data class AppLinksPref(
    @SerializedName("short")
    val short: String? = null,

    @SerializedName("uniqname")
    val uniqname: String? = null
)
