package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity

import com.google.gson.annotations.SerializedName

sealed class AddFavoriteBody {

    data class AddFavoriteByMessageBody(
        @SerializedName("room_id") val roomId: Long,
        @SerializedName("message_id") val messageId: String,
        @SerializedName("attachment_index") val attachmentIndex: Int? = null
    ) : AddFavoriteBody()

    data class AddFavoriteByGifBody(
        @SerializedName("url") val url: String,
        @SerializedName("preview") val preview: String,
        @SerializedName("gif_id") val gifId: String,
        @SerializedName("ratio") val ratio: Float?
    ) : AddFavoriteBody()

    data class AddFavoriteByStickerBody(
        @SerializedName("sticker_id") val stickerId: Int
    ) : AddFavoriteBody()

}
