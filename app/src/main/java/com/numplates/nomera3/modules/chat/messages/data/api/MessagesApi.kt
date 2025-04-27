package com.numplates.nomera3.modules.chat.messages.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.chat.data.GreetingRequestBodyDto
import com.numplates.nomera3.modules.chat.messages.data.EditMessageDtos
import com.numplates.nomera3.modules.chat.messages.data.entity.ForwardMessageEntityResponse
import com.numplates.nomera3.modules.chat.messages.data.entity.MessagesDto
import com.numplates.nomera3.modules.chat.messages.data.entity.SendMultipleParams
import com.numplates.nomera3.modules.chat.messages.data.entity.SendMultipleResponse
import com.numplates.nomera3.modules.userprofile.data.entity.GreetingResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface MessagesApi {

    /**
     * Не используется в настоящий момент.
     * Будет использоваться после рефакторинга чата вместо
     * аналогичного метода из ApiMain.kt
     */
    @Deprecated("Not used at this time")
    @POST("/v2/messages/new_message")
    suspend fun sendNewMessage(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>

    @GET("/v2/messages")
    suspend fun getMessages(
        @Query("room_id") roomId: Long,
        @Query("direction") direction: String,
        @Query("ts") ts: Long?,
        @Query("limit") limit: Int?,
        @Query("user_type") userType: String
    ): ResponseWrapper<MessagesDto>


    /**
     * @return - список названий чатов, для отображения в плашке
     */
    @FormUrlEncoded
    @POST("/v2/messages/resend")
    suspend fun forwardMessage(
        @Field("id") messageId: String,
        @Field("room_id") roomId: Long,
        @Field("user_ids[]") userIds: List<Long>?,
        @Field("room_ids[]") roomIds: List<Long>?,
        @Field("comment") comment: String
    ): ResponseWrapper<ForwardMessageEntityResponse>

    @POST("/v2/messages/send_multiple")
    suspend fun sendMultipleMessages(
        @Body params: SendMultipleParams
    ): ResponseWrapper<SendMultipleResponse>

    @POST("/v2/messages/greeting")
    suspend fun sendGreeting(
        @Body greetingRequestBodyDto: GreetingRequestBodyDto
    ): ResponseWrapper<GreetingResponse>

    @PATCH("v2/messages/edit")
    suspend fun editMessage(
        @Body editMessageRequestBody: EditMessageDtos.Request
    ): ResponseWrapper<EditMessageDtos.Response>
}
