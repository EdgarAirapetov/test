package com.numplates.nomera3.modules.post_view_statistic.data.net

import com.google.gson.annotations.SerializedName

data class PostViewRestRequest(
    @Suppress("detekt:UnusedPrivateMember")
    @SerializedName("posts")
    private val posts: List<PostViewRestData>
)
