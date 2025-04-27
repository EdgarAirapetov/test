package com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import javax.inject.Inject

class GetMediaKeyboardRecentMediaUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(): List<MediakeyboardFavoriteRecentModel> = mediakeyboardRepository.getRecents(MediaKeyboardRecentType.MEDIA)

}
