package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickersAndRecentStickersModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubscribeStickersFlow @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    fun invoke(): Flow<MediaKeyboardStickersAndRecentStickersModel> = mediakeyboardRepository.getStickersFlow()

}
