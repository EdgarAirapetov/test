package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.mapper

import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity.MediaKeyboardStickerDto
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity.MediaKeyboardStickerPackDto
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerPackModel
import javax.inject.Inject

class MediaKeyboardStickerDataMapper @Inject constructor() {

    fun mapStickerPackDtoToDomainModel(src: MediaKeyboardStickerPackDto): MediaKeyboardStickerPackModel {
        return MediaKeyboardStickerPackModel(
            id = src.id,
            title = src.title,
            preview = src.preview,
            createdAt = src.createdAt,
            viewed = src.viewed == 1,
            isNew = src.isNew == 1,
            stickers = src.stickers.map { mapStickerDtoToDomainModel(it, src) },
            useCount = src.useCount
        )
    }

    private fun mapStickerDtoToDomainModel(
        src: MediaKeyboardStickerDto,
        stickerPack: MediaKeyboardStickerPackDto
    ): MediaKeyboardStickerModel {
        return MediaKeyboardStickerModel(
            id = src.id,
            title = src.title,
            url = src.asset.url,
            lottieUrl = src.asset.lottieUrl,
            webpUrl = src.asset.webpUrl,
            emoji = src.emoji,
            keywords = src.keywords,
            stickerPackId = stickerPack.id,
            stickerPackTitle = stickerPack.title
        )
    }

}
