package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class GroupDto(

    @SerializedName("id")
    val groupId: Long,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("count_members")
    val countMembers: Int? = 0
)
