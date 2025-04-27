package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.UserInfoModel
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable

data class CommunityMembersEntity(

    @SerializedName("total_count")
    val totalCount: Int?,

    @SerializedName("friends")
    var friends: List<UserInfoModel>?,

    @SerializedName("users")
    var users: List<UserInfoModel>?

) : Serializable, ListResponse<UserInfoModel>() {

    override fun getList(): List<UserInfoModel>? {
        return friends
    }
}