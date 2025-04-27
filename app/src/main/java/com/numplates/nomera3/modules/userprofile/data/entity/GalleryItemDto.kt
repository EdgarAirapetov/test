package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.Post

data class GalleryItemDto(
    @SerializedName("created_at") val createdAt:Long,
    @SerializedName("id") val id:Long,
    @SerializedName("link") val link:String,
    @SerializedName("post") val post: Post?,
    @SerializedName("post_id") val postId:Long?,
    @SerializedName("is_adult") val isAdult:Int?,
)
