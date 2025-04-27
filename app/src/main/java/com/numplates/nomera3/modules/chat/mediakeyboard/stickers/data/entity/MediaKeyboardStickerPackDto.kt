package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity

import com.google.gson.annotations.SerializedName

data class MediaKeyboardStickerPackDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("preview")
    val preview: String,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("viewed")
    var viewed: Int,
    @SerializedName("is_new")
    var isNew: Int,
    @SerializedName("stickers")
    val stickers: List<MediaKeyboardStickerDto>,
    var useCount: Int = 0,
)
