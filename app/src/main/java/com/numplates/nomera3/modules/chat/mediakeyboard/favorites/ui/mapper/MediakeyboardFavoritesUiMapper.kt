package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import javax.inject.Inject

class MediakeyboardFavoritesUiMapper @Inject constructor() {

    fun mapDomainToUiModel(src: MediakeyboardFavoriteRecentModel): MediakeyboardFavoriteRecentUiModel =
        MediakeyboardFavoriteRecentUiModel(
            id = src.id,
            type = when(src.type) {
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF.value ->
                    MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.IMAGE.value ->
                    MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.IMAGE
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.VIDEO.value ->
                    MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.VIDEO
                MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER.value ->
                    MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER
                else -> error("No such type of favorite.")
            },
            url = src.url,
            preview = src.preview,
            duration = src.duration,
            ratio = src.ratio,
            lottieUrl = src.lottieUrl,
            webpUrl = src.webpUrl,
            emoji = src.emoji,
            favoriteId = src.favoriteId,
            stickerId = src.stickerId,
            messageId = src.messageId,
            gifId = src.gifId,
            stickerCategory = src.stickerPackTitle
        )

}
