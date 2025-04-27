package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity

import com.google.gson.annotations.SerializedName

data class MediakeyboardFavoriteRecentAssetDto(
    @SerializedName("url") val url: String,
    @SerializedName("metadata") val metadata: MediakeyboardFavoriteRecentAssetMetadataDto?,
    @SerializedName("lottie_url") val lottieUrl: String?,
    @SerializedName("webp_url") val webpUrl: String?,
    @SerializedName("favourite_id") val favoriteId: Long?
)

data class MediakeyboardFavoriteRecentAssetMetadataDto(
    @SerializedName("duration") val duration: Int?,
    @SerializedName("preview") val preview: String?,
    @SerializedName("ratio") val ratio: Float?,
    @SerializedName("emoji") val emoji: String?
)
