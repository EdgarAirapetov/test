package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity

import com.google.gson.annotations.SerializedName

data class MediaKeyboardStickerAssetDto(
    @SerializedName("url")
    val url: String,
    @SerializedName("lottie_url")
    val lottieUrl: String,
    @SerializedName("webp_url")
    val webpUrl: String
)
