package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import javax.inject.Inject

class LoadFavoritesUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(
        startId: Int? = null,
        isForMoments: Boolean = false
    ): List<MediakeyboardFavoriteRecentModel> {
        return mediakeyboardRepository.loadFavorites(startId, isForMoments)
    }

}
