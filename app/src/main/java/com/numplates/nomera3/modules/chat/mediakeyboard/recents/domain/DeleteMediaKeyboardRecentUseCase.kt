package com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import javax.inject.Inject

class DeleteMediaKeyboardRecentUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(recentId: Int, type: MediaKeyboardRecentType, isForMoment: Boolean) =
        mediakeyboardRepository.deleteRecent(recentId, type, isForMoment)

}
