package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class ClearMediaKeyboardStickerPacksUseCase @Inject constructor(
    private val mediaKeyboardRepository: MediakeyboardRepository
) {

    fun invoke() = mediaKeyboardRepository.clearStickers()

}
