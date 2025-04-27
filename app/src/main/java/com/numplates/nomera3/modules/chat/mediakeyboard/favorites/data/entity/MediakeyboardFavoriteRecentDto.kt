package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity

import com.google.gson.annotations.SerializedName

data class MediakeyboardFavoriteRecentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("asset") val asset: MediakeyboardFavoriteRecentAssetDto,
    @SerializedName("metadata") val metadata: MediakeyboardFavoriteRecentMetadataDto?
)
