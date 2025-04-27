package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain

import androidx.paging.DataSource
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.AddFavoriteBody
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerPackModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickersAndRecentStickersModel
import kotlinx.coroutines.flow.Flow

interface MediakeyboardRepository {

    fun getAllFavoritesFlow(isForMoments: Boolean): Flow<List<MediakeyboardFavoriteRecentModel>>

    fun getAllFavorites(isForMoments: Boolean): DataSource.Factory<Int, MediakeyboardFavoriteRecentModel>

    suspend fun loadFavorites(startId: Int?, isForMoments: Boolean): List<MediakeyboardFavoriteRecentModel>

    suspend fun addFavorite(body: AddFavoriteBody, isForMoments: Boolean)

    suspend fun saveFavoritesToDb(favorites: List<MediakeyboardFavoriteRecentModel>)

    suspend fun deleteFavorite(favoriteId: Int, isForMoments: Boolean)

    suspend fun deleteAllFavorites()

    suspend fun getRecents(type: MediaKeyboardRecentType): List<MediakeyboardFavoriteRecentModel>

    suspend fun deleteRecent(
        recentId: Int,
        type: MediaKeyboardRecentType,
        forMoments: Boolean
    )

    suspend fun clearRecents(type: MediaKeyboardRecentType, isForMoment: Boolean)

    suspend fun getStickers(): MediaKeyboardStickersAndRecentStickersModel

    suspend fun setStickerPackViewed(id: Int)

    fun clearStickers()

    suspend fun updateSticker(packId: Int): Any

    fun getStickersFlow(): Flow<MediaKeyboardStickersAndRecentStickersModel>

    suspend fun reloadStickersInFlow(isForMoments: Boolean)

    suspend fun reloadRecentStickersInFlow(isForMoments: Boolean)

    fun getCachedStickers(): List<MediaKeyboardStickerPackModel>
}
