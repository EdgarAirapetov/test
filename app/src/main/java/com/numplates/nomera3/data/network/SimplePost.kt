package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

abstract class SimplePost : SimpleUser() {
    abstract var text: String?
    abstract var respName: String?
    abstract var date: Long
    abstract var id: Long

    @SerializedName("image")
    var image: String? = null

    @SerializedName("aspect")
    var aspect: Double = 0.toDouble()

    @SerializedName("comments_count")
    var commentsCount: Int = 0

    @SerializedName("post_city_id")
    var postCityId: Int = 0

}