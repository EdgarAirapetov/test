package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class PostAdEntityResponse(
        @SerializedName("id")
        val id: Long,

        @SerializedName("title")
        val title: String?,

        @SerializedName("text")
        val text: String?,

        @SerializedName("image")
        val image: String?,

        @SerializedName("aspect")
        val aspect: Int,

        @SerializedName("date")
        val date: Long,

        @SerializedName("city_id")
        val cityId: Int,

        @SerializedName("click_payment")
        val clickPayment: Int,

        @SerializedName("limit_payment")
        val limitPayment: Int,

        @SerializedName("link")
        val link: String?,

        @SerializedName("phone")
        val phone: String?,

        @SerializedName("action_type")
        val actionType: String?,

        @SerializedName("showed_sum")
        val showedSum: Int,

        @SerializedName("clicked_sum")
        val clickedSum: Int,

        @SerializedName("avatar")
        val avatar: String?
)
