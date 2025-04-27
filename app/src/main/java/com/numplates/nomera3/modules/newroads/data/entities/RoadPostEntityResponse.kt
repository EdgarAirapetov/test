package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.FeatureEntity
import com.numplates.nomera3.data.network.PostAds

/**
 * Server response get road posts
 */
class RoadPostEntityResponse(
        @SerializedName("posts")
        var posts: MutableList<String?>?,

        @SerializedName("ads")
        var ads: PostAds?,

        @SerializedName("features")
        var features: MutableList<FeatureEntity>?
)
