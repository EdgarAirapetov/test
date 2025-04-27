package com.numplates.nomera3.modules.search.data.entity

import com.google.gson.annotations.SerializedName


data class RecentGroupEntityResponse(

        @SerializedName("type")
        val type: String?,

        @SerializedName("data")
        val data: RecentGroup?,

        @SerializedName("happened_at")
        val happenedAt: Long?
)


data class RecentGroup(

        @SerializedName("id")
        val id: Int?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("avatar")
        val avatar: String?,

        @SerializedName("avatar_big")
        val avatarBig: String?,

        @SerializedName("private")
        val private: Int?,

        @SerializedName("royalty")
        val royalty: Int?,
)
