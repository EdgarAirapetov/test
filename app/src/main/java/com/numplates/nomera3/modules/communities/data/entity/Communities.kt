package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable

data class Communities(

    @SerializedName("total_count")
    val totalCount: Int?,

    @SerializedName("groups")
    var communityEntities: List<CommunityEntity?>?

) : Serializable, ListResponse<CommunityEntity?>() {

    var isNewList: Boolean = false

    override fun getList(): List<CommunityEntity?>? {
        return communityEntities
    }
}