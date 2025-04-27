package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName

data class CommunityShareEntity(

    @SerializedName("group_id")
    var id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("avatar")
    val avatar: String?,

    @SerializedName("private")
    val private: Int,

    @SerializedName("deleted")
    val deleted: Int
)