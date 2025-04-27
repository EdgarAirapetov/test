package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickersAndRecentStickersModel
import javax.inject.Inject

class GetMediaKeyboardStickersUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(): MediaKeyboardStickersAndRecentStickersModel = mediakeyboardRepository.getStickers()

}
