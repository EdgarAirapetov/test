package com.numplates.nomera3.modules.gifservice.data.repository

import com.numplates.nomera3.modules.gifservice.data.entity.GiphyFullResponse
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyItemResponse
import com.meera.db.models.RecentGifEntity

interface GiphyRepository {

    suspend fun search(
            query: String,
            limit: Int,
            offset: Int,
            lang: String,
            success: (GiphyFullResponse) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun getTrending(
            limit: Int,
            offset: Int,
            success: (List<GiphyItemResponse?>) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun setGifToRecent(
            id: String,
            smallUrl: String,
            originalUrl: String,
            originalAspectRatio: Double,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun getRecentGifs(
            success: (urls: List<RecentGifEntity>) -> Unit,
            fail: (Exception) -> Unit
    )
}
