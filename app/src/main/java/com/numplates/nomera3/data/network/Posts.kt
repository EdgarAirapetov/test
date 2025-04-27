package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable

data class Posts(
        @SerializedName("posts") var posts: List<Post?>?,
        @SerializedName("ads") var ads: PostAds?
): Serializable, ListResponse<Post?>() {
    override fun getList(): List<Post?>? {
        return posts
    }
}
