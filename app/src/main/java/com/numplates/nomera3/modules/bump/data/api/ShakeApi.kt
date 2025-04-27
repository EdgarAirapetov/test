package com.numplates.nomera3.modules.bump.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

interface ShakeApi {

    @POST("/v3/shake")
    suspend fun setShakeCoordinates(
        @Query("gps_x") gpsX: Float,
        @Query("gps_y") gpsY: Float
    ) : ResponseWrapper<Any>

    @DELETE("/v3/shake")
    suspend fun deleteShakeUser() : ResponseWrapper<Any>
}
