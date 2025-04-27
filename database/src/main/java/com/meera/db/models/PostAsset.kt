package com.meera.db.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostAsset(
    @SerializedName("metadata")
    val metadata: PostAssetMetadata?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("url")
    val url: String?
) : Serializable {
    companion object {
        fun buildEmpty(): PostAsset {
            return PostAsset(
                metadata = null,
                type = null,
                url = null
            )
        }
    }
}

data class MomentAsset(
    @SerializedName("type")
    val type: String?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("url")
    val url: String?
) : Serializable

data class PostAssetMetadata(

    @SerializedName("aspect")
    val aspect: Float?,

    @SerializedName("low_quality")
    val lowQuality: String?,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("duration")
    val duration: Int?
) : Serializable
