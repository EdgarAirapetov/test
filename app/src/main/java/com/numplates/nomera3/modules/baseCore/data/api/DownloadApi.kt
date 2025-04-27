package com.numplates.nomera3.modules.baseCore.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

//Используется для загрузок файлов c бэка на устройство
interface DownloadApi {
    @GET("/v2/share/video")
    @Streaming
    suspend fun downloadPostVideo(@Query("post_id") postId: Long): ResponseBody

    @GET("/v2/share/video")
    @Streaming
    suspend fun downloadPostVideoByAsset(@Query("post_id") postId: Long, @Query("asset_id") assetId: String): ResponseBody
}
