package com.meera.db.models.userprofile

import com.google.gson.annotations.SerializedName

data class GroupEntity(

    @SerializedName("id")
    val groupId: Long,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("count_members")
    val countMembers: Int? = 0
)
