package com.numplates.nomera3.modules.post_view_statistic.data.net

import com.google.gson.annotations.SerializedName

data class FeatureViewRestRequest(
    @Suppress("detekt:UnusedPrivateMember")
    @SerializedName("features")
    private val features: List<PostViewRestData>
)
