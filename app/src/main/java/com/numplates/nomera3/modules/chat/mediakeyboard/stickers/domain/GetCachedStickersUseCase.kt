package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class GetCachedStickersUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    fun invoke() = mediakeyboardRepository.getCachedStickers()

}
