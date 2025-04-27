package com.numplates.nomera3.modules.user.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface UserComplainApi {

    @POST("/v2/complaints")
    suspend fun complainOnUser(@Body complainBody: HashMap<String, Any>): ResponseWrapper<Any?>?

    @POST("/v2/complaints")
    suspend fun complainOnUserFromChat(
        @Query("user_id") userId: Long,
        @Query("reason_id") reasonId: Int,
        @Query("room_id") roomId: Long
    ): ResponseWrapper<Any?>?
}
