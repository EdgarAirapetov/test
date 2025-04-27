package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.PostAds

/**
 * Response from server subscriptions posts
 */
data class SubscriptionsPostEntityResponse(

        @SerializedName("posts")
        var posts: List<String?>?,

        @SerializedName("ads")
        var ads: PostAds?
)
