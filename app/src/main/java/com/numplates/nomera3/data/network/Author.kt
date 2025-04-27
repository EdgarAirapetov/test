package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Author(
        @SerializedName("id") var id: Long,
        @SerializedName("number") var number: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("avatar") var avatar: String?,
        @SerializedName("avatar_date") var avatarDate: Int,
        @SerializedName("city_id") var cityId: Int,
        @SerializedName("verified") var verified: Int,
        @SerializedName("vehicle") var vehicle: Int
): Serializable