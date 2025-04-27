package com.numplates.nomera3.modules.music.data.repository

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.music.data.api.MusicApi
import com.numplates.nomera3.modules.music.data.mapper.MusicSearchResponseMapper
import com.numplates.nomera3.modules.music.domain.MusicRepository
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import timber.log.Timber
import javax.inject.Inject

const val EMPTY_RESPONSE = "Empty response"

class MusicRepositoryImpl @Inject constructor(
    private val api: MusicApi,
    private val mapper: MusicSearchResponseMapper
) : MusicRepository {

    override suspend fun searchMusicTest(
        limit: Int,
        offset: Int,
        query: String
    ): List<MusicSearchEntity> {
        return try {
            val result = api.searchMusic(
                query = query,
                limit = limit,
                offset = offset
            )
            if (result.data == null) error(EMPTY_RESPONSE)
            mapper.mapRespToDomainModel(result.data)
        } catch (e: Exception) {
            Timber.e(e)
            throw RuntimeException("Search music failed")
        }
    }

    override suspend fun requestTopMusic(limit: Int, offset: Int): List<MusicSearchEntity> {
        return try {
            val result = api.searchMusic(
                query = String.empty(),
                limit = limit,
                offset = offset
            )
            if (result.data == null) error(EMPTY_RESPONSE)
            mapper.mapRespToDomainModel(result.data)
        } catch (e: Exception) {
            Timber.e(e)
            throw RuntimeException("Request top music failed")
        }
    }
}
