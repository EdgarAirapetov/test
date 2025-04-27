package com.numplates.nomera3.modules.gift_coffee.data.entity

import com.google.gson.annotations.SerializedName

data class GiftPlaceEntityResponse(

        @SerializedName("id")
        val id: Int?,

        @SerializedName("title")
        val title: String?,

        @SerializedName("address")
        val address: String?,

        @SerializedName("image")
        val image: String?
)
