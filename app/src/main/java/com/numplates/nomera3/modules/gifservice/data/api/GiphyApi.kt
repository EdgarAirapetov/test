package com.numplates.nomera3.modules.gifservice.data.api

import com.numplates.nomera3.modules.gifservice.data.entity.GiphyResponseWrapper
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyItemResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * https://developers.giphy.com/docs/api/endpoint
 */
interface GiphyApi {

    @GET("/v1/gifs/search")
    suspend fun search(
            @Query("api_key") apiKey: String,
            @Query("q") query: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int,
            @Query("lang") lang: String
    ): GiphyResponseWrapper<GiphyItemResponse>

    @GET("/v1/gifs/trending")
    suspend fun getTrending(
            @Query("api_key") apiKey: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int,
    ): GiphyResponseWrapper<GiphyItemResponse>

}