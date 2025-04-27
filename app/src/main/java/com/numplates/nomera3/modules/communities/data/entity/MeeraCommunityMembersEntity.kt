package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.MeeraUserInfoModel
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable

data class MeeraCommunityMembersEntity(

    @SerializedName("total_count")
    val totalCount: Int?,

    @SerializedName("friends")
    var friends: List<MeeraUserInfoModel>?,

    @SerializedName("users")
    var users: List<MeeraUserInfoModel>?

) : Serializable, ListResponse<MeeraUserInfoModel>() {

    override fun getList(): List<MeeraUserInfoModel>? {
        return friends
    }
}
