package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName

data class UpdateUserProfileResponse(

        @SerializedName("id")
        val id: Long,

        @SerializedName("avatar_big")
        val avatarBig: String,

        @SerializedName("avatar_small")
        val avatarSmall: String,

        @SerializedName("type")
        val type: Int,

        @SerializedName("birthday")
        val birthday: Long? = 0L,

        @SerializedName("color")
        val color: Int,

        @SerializedName("gender")
        val gender: Int,

        @SerializedName("city_id")
        val cityId: Int,

        @SerializedName("status")
        val status: String,

        @SerializedName("name")
        val name: String,

        @SerializedName("email")
        val email: String,

        @SerializedName("phone")
        val phone: String,

        @SerializedName("country_id")
        val countryId: Int

)