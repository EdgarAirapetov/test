package com.numplates.nomera3.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiFileStorage {

    @Streaming
    @GET
    suspend fun downloadFileFromUrl(@Url url: String): ResponseBody
}
