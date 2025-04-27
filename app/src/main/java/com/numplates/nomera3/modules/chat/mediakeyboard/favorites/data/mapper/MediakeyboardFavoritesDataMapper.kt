package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.mapper

import com.meera.db.models.MediakeyboardFavoriteDbModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.MediakeyboardFavoriteRecentDto
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import javax.inject.Inject

class MediakeyboardFavoritesDataMapper @Inject constructor() {

    fun mapDtoToDomainModel(
        src: MediakeyboardFavoriteRecentDto,
        isFromMoments: Boolean = false
    ): MediakeyboardFavoriteRecentModel =
        MediakeyboardFavoriteRecentModel(
            id = src.id,
            type = src.type,
            url = src.asset.url,
            preview = src.asset.metadata?.preview ?: src.asset.url,
            duration = src.asset.metadata?.duration,
            ratio = src.asset.metadata?.ratio,
            lottieUrl = src.asset.lottieUrl,
            webpUrl = src.asset.webpUrl,
            emoji = src.asset.metadata?.emoji,
            favoriteId = src.asset.favoriteId,
            stickerId = src.metadata?.stickerId,
            messageId = src.metadata?.messageId,
            gifId = src.metadata?.gifId,
            stickerPackTitle = src.metadata?.stickerPackTitle,
            isFromMoments = isFromMoments
        )

    fun mapDomainToDbModel(src: MediakeyboardFavoriteRecentModel): MediakeyboardFavoriteDbModel =
        MediakeyboardFavoriteDbModel(
            id = src.id,
            type = src.type,
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
            stickerPackTitle = src.stickerPackTitle,
            isFromMoments = src.isFromMoments
        )

    fun mapDbToDomainModel(src: MediakeyboardFavoriteDbModel): MediakeyboardFavoriteRecentModel =
        MediakeyboardFavoriteRecentModel(
            id = src.id,
            type = src.type,
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
            stickerPackTitle = src.stickerPackTitle,
            isFromMoments = src.isFromMoments
        )

}
