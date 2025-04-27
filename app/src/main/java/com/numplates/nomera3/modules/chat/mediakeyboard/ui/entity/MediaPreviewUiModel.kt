package com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.AddFavoriteBody
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel

sealed class MediaUiModel(val id: Int?) {
    data class GifMediaUiModel(
        val mediaId: Int? = null,
        val preview: String,
        val url: String,
        val gifId: String? = null,
        val ratio: Float? = null,
        val roomId: Long? = null,
        val messageId: String? = null
    ) : MediaUiModel(mediaId) {
        override fun toAddFavoriteBody(): AddFavoriteBody? {
            if (roomId != null && messageId != null) {
                return AddFavoriteBody.AddFavoriteByMessageBody(
                    roomId = roomId,
                    messageId = messageId
                )
            }
            val gifId = gifId ?: return null
            return AddFavoriteBody.AddFavoriteByGifBody(
                url = url,
                preview = preview,
                gifId = gifId,
                ratio = ratio
            )
        }
    }

    data class ImageMediaUiModel(
        val mediaId: Int? = null,
        val url: String,
        val roomId: Long? = null,
        val messageId: String? = null,
        val attachmentIndex: Int? = null
    ) : MediaUiModel(mediaId) {
        override fun toAddFavoriteBody(): AddFavoriteBody? {
            val roomId = this.roomId ?: return null
            val messageId = this.messageId ?: return null
            return AddFavoriteBody.AddFavoriteByMessageBody(
                roomId = roomId,
                messageId = messageId,
                attachmentIndex = attachmentIndex
            )
        }
    }

    data class VideoMediaUiModel(
        val mediaId: Int? = null,
        val url: String,
        val preview: String? = null,
        val roomId: Long? = null,
        val messageId: String? = null,
        val duration: Int? = null,
        val attachmentIndex: Int? = null
    ) : MediaUiModel(mediaId) {
        override fun toAddFavoriteBody(): AddFavoriteBody? {
            val roomId = this.roomId ?: return null
            val messageId = this.messageId ?: return null
            return AddFavoriteBody.AddFavoriteByMessageBody(
                roomId = roomId,
                messageId = messageId,
                attachmentIndex = attachmentIndex
            )
        }
    }

    data class StickerMediaUiModel(
        val favoriteId: Int? = null,
        val stickerId: Int? = null,
        val stickerUrl: String,
        val lottieUrl: String? = null,
        val webpUrl: String? = null,
        val roomId: Long? = null,
        val messageId: String? = null,
        val stickerPackTitle: String? = null
    ) : MediaUiModel(favoriteId) {
        override fun toAddFavoriteBody(): AddFavoriteBody? {
            if (roomId != null && messageId != null) {
                return AddFavoriteBody.AddFavoriteByMessageBody(
                    roomId = roomId,
                    messageId = messageId
                )
            }
            val stickerId = stickerId ?: return null
            return AddFavoriteBody.AddFavoriteByStickerBody(stickerId)
        }
    }

    abstract fun toAddFavoriteBody(): AddFavoriteBody?

    companion object {
        fun fromMediakeyboardFavoriteRecentUiModel(model: MediakeyboardFavoriteRecentUiModel, roomId: Long?): MediaUiModel {
            return when (model.type) {
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.IMAGE -> ImageMediaUiModel(
                    mediaId = model.id,
                    url = model.url,
                    messageId = model.messageId,
                    roomId = roomId
                )
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.VIDEO -> VideoMediaUiModel(
                    mediaId = model.id,
                    url = model.url,
                    preview = model.preview,
                    duration = model.duration,
                    messageId = model.messageId,
                    roomId = roomId
                )
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF ->
                    GifMediaUiModel(
                        mediaId = model.id,
                        preview = model.preview,
                        url = model.url,
                        gifId = model.gifId,
                        ratio = model.ratio
                    )
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER -> StickerMediaUiModel(
                    favoriteId = model.id,
                    stickerId = model.stickerId,
                    stickerUrl = model.preview,
                    lottieUrl = model.lottieUrl,
                    webpUrl = model.webpUrl,
                    stickerPackTitle = model.stickerCategory
                )
            }
        }
    }

}

data class MediaPreviewUiModel(
    val media: MediaUiModel,
    val type: MediaPreviewType,
    val isAdded: Boolean,
    val favoriteRecentModel: MediakeyboardFavoriteRecentUiModel? = null
)

enum class MediaPreviewType {
    FAVORITE, RECENT, STICKER, GIPHY
}
