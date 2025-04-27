package com.numplates.nomera3.modules.appInfo.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import retrofit2.http.GET

interface AppInfoApi {

    @GET("/v1/info/application?type=android")
    suspend fun getApplicationInfo(): ResponseWrapper<Settings?>

}