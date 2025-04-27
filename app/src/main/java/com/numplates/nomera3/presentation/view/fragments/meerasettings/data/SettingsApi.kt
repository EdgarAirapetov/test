package com.numplates.nomera3.presentation.view.fragments.meerasettings.data

import com.numplates.nomera3.data.network.PushSettingsResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface SettingsApi {
    @PATCH("/v1/users/{user_id}/settings")
    suspend fun updatePushSettings(
        @Body body: PushSettingsResponse?,
        @Path("user_id") userID: Long
    ): ResponseWrapper<PushSettingsResponse>

    //получение настроек пушей
    @GET("/v1/users/{user_id}/settings")
    suspend fun getPushSettings(
        @Path("user_id") userID: Long
    ): ResponseWrapper<PushSettingsResponse>
}
