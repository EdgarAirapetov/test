package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName

data class GroupAuthorEntityResponse(

        @SerializedName("id")
        val id: Long?,

        @SerializedName("number")
        val number: String?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("avatar")
        val avatar: String?,

        @SerializedName("avatar_date")
        val avatarDate: Int?,

        @SerializedName("city_id")
        val cityId: Int?,

        @SerializedName("verified")
        val verified: Int?,

        @SerializedName("vehicle")
        val vehicle: Int?
)
