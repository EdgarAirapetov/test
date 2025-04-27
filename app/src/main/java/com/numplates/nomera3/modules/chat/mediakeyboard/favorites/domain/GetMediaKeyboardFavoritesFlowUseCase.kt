package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaKeyboardFavoritesFlowUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    fun invoke(isForMoments: Boolean = false): Flow<List<MediakeyboardFavoriteRecentModel>> =
        mediakeyboardRepository.getAllFavoritesFlow(isForMoments)

}
