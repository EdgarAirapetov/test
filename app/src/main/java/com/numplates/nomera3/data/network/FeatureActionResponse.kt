package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class FeatureActionResponse(
    @SerializedName("action")
    val action: String,
    @SerializedName("feature_id")
    val feature_id: Int,
    @SerializedName("inserted_at")
    val inserted_at: String,
    @SerializedName("user_id")
    val user_id: Long)