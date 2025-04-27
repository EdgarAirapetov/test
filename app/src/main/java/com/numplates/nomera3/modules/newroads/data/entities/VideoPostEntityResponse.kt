package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.PostAds

data class VideoPostEntityResponse(
    @SerializedName("posts")
    var posts: List<String?>?,

    @SerializedName("ads")
    var ads: PostAds?
)
