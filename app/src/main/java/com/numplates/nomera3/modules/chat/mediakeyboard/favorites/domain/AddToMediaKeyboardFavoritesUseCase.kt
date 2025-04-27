package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.AddFavoriteBody
import javax.inject.Inject

class AddToMediaKeyboardFavoritesUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    suspend fun invoke(body: AddFavoriteBody, isForMoments: Boolean = false) {
        mediakeyboardRepository.addFavorite(body, isForMoments)
    }

}
