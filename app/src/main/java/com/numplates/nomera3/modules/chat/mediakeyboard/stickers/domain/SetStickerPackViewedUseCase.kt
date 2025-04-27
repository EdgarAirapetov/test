package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class SetStickerPackViewedUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(id: Int) = mediakeyboardRepository.setStickerPackViewed(id)

}
