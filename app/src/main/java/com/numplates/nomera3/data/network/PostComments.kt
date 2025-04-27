package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable

data class PostComments(
        @SerializedName("comments") var comments: List<Comment>?,
        @SerializedName("read_events") var readEvents: Int
): Serializable, ListResponse<Comment>(){
    override fun getList(): List<Comment>? {
        return comments
    }
}