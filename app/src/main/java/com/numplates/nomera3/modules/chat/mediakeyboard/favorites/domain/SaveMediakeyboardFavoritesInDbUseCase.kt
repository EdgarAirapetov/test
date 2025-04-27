package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import javax.inject.Inject

class SaveMediakeyboardFavoritesInDbUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(favorites: List<MediakeyboardFavoriteRecentModel>) {
        mediakeyboardRepository.saveFavoritesToDb(favorites)
    }

}
