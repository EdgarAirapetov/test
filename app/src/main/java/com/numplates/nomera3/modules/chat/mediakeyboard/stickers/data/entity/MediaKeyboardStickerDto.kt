package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity

import com.google.gson.annotations.SerializedName

data class MediaKeyboardStickerDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("asset")
    val asset: MediaKeyboardStickerAssetDto,
    @SerializedName("emoji")
    val emoji: List<String>,
    @SerializedName("keywords")
    val keywords: List<String>,
    @SerializedName("title")
    val title: String,
)
