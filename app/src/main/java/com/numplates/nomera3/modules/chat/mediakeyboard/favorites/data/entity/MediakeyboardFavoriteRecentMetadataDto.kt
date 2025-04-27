package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity

import com.google.gson.annotations.SerializedName

data class MediakeyboardFavoriteRecentMetadataDto(
    @SerializedName("sticker_id") val stickerId: Int?,
    @SerializedName("sticker_pack_title") val stickerPackTitle: String?,
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("gif_id") val gifId: String?
)
