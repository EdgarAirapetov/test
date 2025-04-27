package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class UpdateStickerOrderUseCase @Inject constructor(
    private val mediaKeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(packId: Int) = mediaKeyboardRepository.updateSticker(packId)
}
