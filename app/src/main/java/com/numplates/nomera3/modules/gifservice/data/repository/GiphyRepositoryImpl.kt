package com.numplates.nomera3.modules.gifservice.data.repository

import com.numplates.nomera3.GIPHY_API_KEY
import com.meera.db.DataStore
import com.numplates.nomera3.modules.gifservice.data.api.GiphyApi
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyFullResponse
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyItemResponse
import com.meera.db.models.RecentGifEntity
import timber.log.Timber
import javax.inject.Inject

class GiphyRepositoryImpl @Inject constructor(
        private val giphyApi: GiphyApi,
        private val dataStore: DataStore
) : GiphyRepository {


    override suspend fun getTrending(
            limit: Int,
            offset: Int,
            success: (List<GiphyItemResponse?>) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            val response = giphyApi.getTrending(GIPHY_API_KEY, limit, offset)
            if (response.data != null) {
                success(response.data)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun search(
            query: String,
            limit: Int,
            offset: Int,
            lang: String,
            success: (GiphyFullResponse) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            val response = giphyApi.search(GIPHY_API_KEY, query, limit, offset, lang)
            if (response.data != null) {
                success(GiphyFullResponse(
                        data = response.data,
                        pagination = response.pagination
                ))
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
            Timber.d(e)
        }
    }

    override suspend fun setGifToRecent(
            id: String,
            smallUrl: String,
            originalUrl: String,
            originalAspectRatio: Double,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            dataStore.recentGifsDao().addGifToRecent(RecentGifEntity(
                    id = id,
                    smallUrl = smallUrl,
                    originalUrl = originalUrl,
                    originalAspectRatio = originalAspectRatio,
                    timestamp = System.currentTimeMillis()
            ))
            success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e)
        }
    }

    override suspend fun getRecentGifs(
            success: (urls: List<RecentGifEntity>) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            val urls = dataStore.recentGifsDao()
                    .getRecentGifs()
            success(urls)
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e)
        }
    }

}
