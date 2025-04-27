package com.numplates.nomera3.modules.chat.toolbar.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.data.newmessenger.response.ChatUsers
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatUserApi {

    @GET("/v2/users/get_user_info")
    suspend fun getUserInfo(
        @Query("user_id") userId: Long,
        @Query("user_type") userType: String,
        @Query("version") version: String
    ): ResponseWrapper<ChatUsers>

    @FormUrlEncoded
    @POST("/v2/users/{user_id}/permissions/message_notification")
    suspend fun setMessageNotificationPermission(
        @Path("user_id") userId: Long,
        @Field("value")  value: Boolean
    ): ResponseWrapper<Any>

}
