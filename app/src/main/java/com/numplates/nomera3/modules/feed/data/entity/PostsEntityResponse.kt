package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.tags.data.entity.HashtagModel

data class PostsEntityResponse(
        @SerializedName("posts")
        val posts: List<PostEntityResponse>? = null,

        @SerializedName("features")
        val features: List<FeatureEntityResponse>? = null,

        @SerializedName("ads")
        val ads: ArrayList<PostAdEntityResponse>? = null,

        @SerializedName("hashtag")
        val hashtag: HashtagModel?
)