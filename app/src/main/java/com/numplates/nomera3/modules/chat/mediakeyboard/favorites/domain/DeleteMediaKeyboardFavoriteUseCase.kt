package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import javax.inject.Inject

class DeleteMediaKeyboardFavoriteUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(favoriteId: Int, isForMoments: Boolean = false) {
        mediakeyboardRepository.deleteFavorite(favoriteId, isForMoments)
    }

}
