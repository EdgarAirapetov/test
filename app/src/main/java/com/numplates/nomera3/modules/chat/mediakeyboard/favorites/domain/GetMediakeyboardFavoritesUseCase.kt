package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import androidx.paging.DataSource
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import javax.inject.Inject

class GetMediakeyboardFavoritesUseCase @Inject constructor(
    private val mediakeyboardRepository: MediakeyboardRepository
) {

    fun invoke(isForMoments: Boolean = false): DataSource.Factory<Int, MediakeyboardFavoriteRecentModel> =
        mediakeyboardRepository.getAllFavorites(isForMoments)

}
