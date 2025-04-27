package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName

data class ResponseGetAlbum(

    @SerializedName("more_items")
    val moreItems: Int,
    val photos: List<Photo>

)

data class Photo(

    @SerializedName("created_at")
    val createdAt: Long,
    val id: Long,
    val image: Image,
    val isAdult: Boolean

)

data class Image(

    @SerializedName("data")
    val imageData: Data,
    @SerializedName("link")
    val url: String

)

data class Data(

    val ratio: String,
    val size: String

)
