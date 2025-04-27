package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class UserSearchByNameModel(
        @SerializedName("account_color")
        var accountColor: Int,
        @SerializedName("account_type")
        var accountType: Int,
        @SerializedName("avatar")
        var avatar: String,
        @SerializedName("birthday")
        var birthday: Long?,
        @SerializedName("city_name")
        var cityName: String,
        @SerializedName("gender")
        var gender: Int,
        @SerializedName("name")
        var name: String,
        @SerializedName("user_id")
        var userId: Int,
        @SerializedName("vehicle")
        var vehicle: VehicleX? = null,
        @SerializedName("uniqname")
        var uniqueName: String? = null,
        @SerializedName("approved")
        var approved: Int = 0
)