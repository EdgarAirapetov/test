package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.mapper

import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerPackModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import javax.inject.Inject

class MediaKeyboardStickerUiMapper @Inject constructor() {

    fun mapStickerPackDomainToUiModel(src: MediaKeyboardStickerPackModel): MediaKeyboardStickerPackUiModel {
        return MediaKeyboardStickerPackUiModel(
            id = src.id,
            title = src.title,
            preview = src.preview,
            createdAt = src.createdAt,
            viewed = src.viewed,
            stickers = src.stickers.map(this::mapStickerDomainToUiModel)
        )
    }

    fun mapStickerDomainToUiModel(src: MediaKeyboardStickerModel): MediaKeyboardStickerUiModel {
        return MediaKeyboardStickerUiModel(
            id = src.id,
            title = src.title,
            url = src.url,
            lottieUrl = src.lottieUrl,
            webpUrl = src.webpUrl,
            emoji = src.emoji,
            keywords = src.keywords,
            stickerPackId = src.stickerPackId,
            stickerPackTitle = src.stickerPackTitle
        )
    }

}
