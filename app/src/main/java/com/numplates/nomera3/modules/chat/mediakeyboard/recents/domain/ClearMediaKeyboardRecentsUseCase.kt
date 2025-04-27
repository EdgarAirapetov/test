package com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class ClearMediaKeyboardRecentsUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(type: MediaKeyboardRecentType, isForMoment: Boolean) =
        mediakeyboardRepository.clearRecents(type, isForMoment)

}
