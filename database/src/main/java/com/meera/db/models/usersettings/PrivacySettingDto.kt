package com.meera.db.models.usersettings

import com.google.gson.annotations.SerializedName

data class PrivacySettingDto(
    @SerializedName("key")
    val key: String,

    @SerializedName("value")
    val value: Int?,

    @SerializedName("count_blacklist")
    val countBlacklist: Int? = null,

    @SerializedName("count_whitelist")
    val countWhitelist: Int? = null
)
