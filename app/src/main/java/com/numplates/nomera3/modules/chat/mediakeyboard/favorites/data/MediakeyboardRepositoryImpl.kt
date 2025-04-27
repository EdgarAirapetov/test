package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data

import androidx.paging.DataSource
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.toInt
import com.meera.db.dao.MediakeyboardFavoritesDao
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.AddFavoriteBody
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.MediakeyboardFavoriteRecentDto
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.mapper.MediakeyboardFavoritesDataMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity.MediaKeyboardStickerPackDto
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.mapper.MediaKeyboardStickerDataMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickerPackModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity.MediaKeyboardStickersAndRecentStickersModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

const val FAVORITES_PAGE_SIZE = 30

@AppScope
class MediakeyboardRepositoryImpl @Inject constructor(
    private val api: ApiMain,
    private val favoritesDao: MediakeyboardFavoritesDao,
    private val favoritesMapper: MediakeyboardFavoritesDataMapper,
    private val stickersMapper: MediaKeyboardStickerDataMapper
) : MediakeyboardRepository {

    private val stickerPacks = mutableListOf<MediaKeyboardStickerPackDto>()
    private val stickerPacksFlow = MutableStateFlow(MediaKeyboardStickersAndRecentStickersModel())
    private val recentStickers = mutableListOf<MediakeyboardFavoriteRecentDto>()

    override fun getAllFavoritesFlow(isForMoments: Boolean): Flow<List<MediakeyboardFavoriteRecentModel>> =
        favoritesDao.getAllFavoritesFlow(isForMoments).map { it.map(favoritesMapper::mapDbToDomainModel) }

    override fun getAllFavorites(isForMoments: Boolean): DataSource.Factory<Int, MediakeyboardFavoriteRecentModel> =
        favoritesDao.getAllFavorites(isForMoments).map(favoritesMapper::mapDbToDomainModel)

    override suspend fun loadFavorites(startId: Int?, isForMoments: Boolean): List<MediakeyboardFavoriteRecentModel> {
        val data = if (isForMoments) {
            api.getMomentMediakeyboardFavorites(startId, FAVORITES_PAGE_SIZE)
        } else {
            api.getMediakeyboardFavorites(startId, FAVORITES_PAGE_SIZE)
        }
        if (data.data == null) error("Error loading favorites.")
        return data.data.map { favoritesMapper.mapDtoToDomainModel(it, isForMoments) }
    }

    override suspend fun saveFavoritesToDb(favorites: List<MediakeyboardFavoriteRecentModel>) {
        favoritesDao.addFavoriteList(favorites.map(favoritesMapper::mapDomainToDbModel))
    }

    override suspend fun addFavorite(body: AddFavoriteBody, isForMoments: Boolean) {
        when (body) {
            is AddFavoriteBody.AddFavoriteByStickerBody -> {
                if (isForMoments) {
                    api.addMomentMediakeyboardFavorite(stickerBody = body)
                } else {
                    api.addMediakeyboardFavorite(stickerBody = body)
                }
            }

            is AddFavoriteBody.AddFavoriteByGifBody -> {
                api.addMediakeyboardFavorite(gifBody = body)
            }

            is AddFavoriteBody.AddFavoriteByMessageBody -> {
                api.addMediakeyboardFavorite(messageBody = body)
            }
        }
    }

    override suspend fun deleteFavorite(favoriteId: Int, isForMoments: Boolean) {
        favoritesDao.deleteFavorite(favoriteId)
        if (isForMoments) {
            api.deleteMomentMediakeyboardFavorite(favoriteId)
        } else {
            api.deleteMediakeyboardFavorite(favoriteId)
        }
    }

    override suspend fun deleteAllFavorites() {
        favoritesDao.deleteAllFavorites()
    }

    override suspend fun getRecents(type: MediaKeyboardRecentType): List<MediakeyboardFavoriteRecentModel> {
        val data = api.getMediaKeyboardRecents(type.value)
        if (data.data == null) error("Error loading recents.")
        return data.data.map(favoritesMapper::mapDtoToDomainModel)
    }

    override suspend fun deleteRecent(
        recentId: Int,
        type: MediaKeyboardRecentType,
        forMoments: Boolean
    ) {
        if (forMoments) {
            api.deleteMomentMediaKeyboardRecent(type.value, recentId)
        } else {
            api.deleteMediaKeyboardRecent(type.value, recentId)
        }
        if (type == MediaKeyboardRecentType.STICKERS) {
            recentStickers.removeIf { it.id == recentId }
            reloadStickersInFlow(forMoments)
        }
    }

    override suspend fun clearRecents(
        type: MediaKeyboardRecentType,
        isForMoment: Boolean
    ) {
        if (isForMoment) {
            api.clearMomentMediaKeyboardRecents(type.value)
        } else {
            api.clearMediaKeyboardRecents(type.value)
        }
        if (type == MediaKeyboardRecentType.STICKERS) {
            recentStickers.clear()
            reloadStickersInFlow(isForMoment)
        }
    }

    override suspend fun getStickers(): MediaKeyboardStickersAndRecentStickersModel {
        if (stickerPacks.isNotEmpty() && recentStickers.isNotEmpty()) {
            return getStickersModel()
        }
        val stickersData = api.getMediaKeyboardStickerPacks()
        val recentStickersData = api.getMediaKeyboardRecents(MediaKeyboardRecentType.STICKERS.value)
        if (stickersData.data == null) error("Error loading stickers.")
        if (recentStickersData.data == null) error("Error loading recent stickers.")
        stickersData.data?.let {
            stickerPacks.clear()
            stickerPacks.addAll(it)
        }
        recentStickersData.data?.let {
            recentStickers.clear()
            recentStickers.addAll(it)
        }
        return getStickersModel()
    }

    override fun getStickersFlow(): Flow<MediaKeyboardStickersAndRecentStickersModel> {
        return stickerPacksFlow
    }

    override suspend fun reloadStickersInFlow(isForMoments: Boolean) {
        if (stickerPacks.isNotEmpty() && recentStickers.isNotEmpty()) {
            stickerPacksFlow.emit(getStickersModel())
        }
        val stickersData = api.getMediaKeyboardStickerPacks()
        val recentStickersData = if (isForMoments) {
            api.getMomentMediaKeyboardRecents(MediaKeyboardRecentType.STICKERS.value)
        } else {
            api.getMediaKeyboardRecents(MediaKeyboardRecentType.STICKERS.value)
        }
        if (stickersData.data == null) {
            Timber.e("Error loading stickers.")
        }
        if (recentStickersData.data == null) {
            Timber.e("Error loading recent stickers.")
        }
        stickersData.data?.let {
            stickerPacks.clear()
            stickerPacks.addAll(it)
        }
        recentStickersData.data?.let {
            recentStickers.clear()
            recentStickers.addAll(it)
        }
        stickerPacksFlow.emit(getStickersModel())
    }

    override suspend fun reloadRecentStickersInFlow(isForMoments: Boolean) {
        stickerPacksFlow.emit(getStickersModel())
        val recentStickersData = if (isForMoments) {
            api.getMomentMediaKeyboardRecents(MediaKeyboardRecentType.STICKERS.value)
        } else {
            api.getMediaKeyboardRecents(MediaKeyboardRecentType.STICKERS.value)
        }
        if (recentStickersData.data == null) {
            Timber.e("Error loading recent stickers.")
            return
        }
        recentStickers.clear()
        recentStickers.addAll(recentStickersData.data)
        stickerPacksFlow.emit(getStickersModel())
    }

    override suspend fun setStickerPackViewed(id: Int) {
        stickerPacks.firstOrNull { it.id == id }?.viewed = true.toInt()
        stickerPacksFlow.emit(getStickersModel())
        api.setMediaKeyboardStickerPackViewed(id)
    }

    override fun clearStickers() {
        stickerPacks.clear()
    }

    override suspend fun updateSticker(packId: Int) = withContext(Dispatchers.Default) {
        stickerPacks.find { it.id == packId }?.useCount = (stickerPacks.find { it.id == packId }?.useCount ?: 0) + 1
        stickerPacksFlow.emit(getStickersModel())
    }

    private fun getStickersModel(): MediaKeyboardStickersAndRecentStickersModel {
        return MediaKeyboardStickersAndRecentStickersModel(
            stickerPacks
                .map(stickersMapper::mapStickerPackDtoToDomainModel)
                .sortedWith(compareByDescending(MediaKeyboardStickerPackModel::isNew).thenByDescending { it.useCount }),
            recentStickers.map(favoritesMapper::mapDtoToDomainModel)
        )
    }

    override fun getCachedStickers(): List<MediaKeyboardStickerPackModel> {
        return stickerPacks.map(stickersMapper::mapStickerPackDtoToDomainModel)
    }
}
