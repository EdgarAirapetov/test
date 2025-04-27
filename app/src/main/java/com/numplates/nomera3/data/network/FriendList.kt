package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse

import java.io.Serializable

/**
 * created by c7j on 12.07.18
 */
data class FriendList(
    @SerializedName("friends") var friends: List<UserInfoModel?>?
) : Serializable, ListResponse<UserInfoModel?>() {
    override fun getList(): List<UserInfoModel?>? {
        return friends
    }
}
