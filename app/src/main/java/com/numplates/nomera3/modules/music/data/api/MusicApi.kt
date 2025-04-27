package com.numplates.nomera3.modules.music.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.music.data.entity.MusicResponseEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {
    @GET("/v2/search/tracks")
    suspend fun searchMusic(
        @Query("query") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<MusicResponseEntity>
}
