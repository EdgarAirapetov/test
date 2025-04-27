package com.numplates.nomera3.modules.fileuploads.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.fileuploads.data.model.ChatAttachmentPartialUploadDto
import com.numplates.nomera3.modules.fileuploads.data.model.CreatePartialUploadResponseDto
import com.numplates.nomera3.modules.fileuploads.data.model.SendPartialUploadDataResponseDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Апи для загрузки файлов на прямую в наш сторадж
 * */
interface ApiUploadStorage{
    @Multipart
    @POST("uploads/partial/append/direct")
    suspend fun uploadFileToStorage(
        @Query("id") partialUploadId: String,
        @Query("offset") offset: Int,
        @Part fileData: MultipartBody.Part
    ): SendPartialUploadDataResponseDto
}

interface ApiUpload {

    @Multipart
    @POST("/v2/rooms/change_avatar")
    suspend fun uploadGroupChatAvatar(
        @Query("target_id") roomId: Long,
        @Part imageFile: MultipartBody.Part?
    ): ResponseWrapper<Any>

    @Multipart
    @POST("/v2/uploads/messengers/chats")
    suspend fun uploadMediaToChat(
        @Part item_1: MultipartBody.Part?,
        @Part item_2: MultipartBody.Part?,
        @Part item_3: MultipartBody.Part?,
        @Part item_4: MultipartBody.Part?,
        @Part item_5: MultipartBody.Part?,
        @Part("target_id") targetID: Long?,
    ): ResponseWrapper<Any>

    @POST("/v2/uploads/partial")
    suspend fun createPartialUpload(
        @Query("size") size: Long,
        @Query("name") fileName: String,
        @Query("parts_count") parts_count: Int? = null,
        @Query("source") source: String? = null
    ): ResponseWrapper<CreatePartialUploadResponseDto>

    @Multipart
    @POST("/v2/uploads/partial/{id}")
    suspend fun sendPartialUploadData(
        @Path("id") partialUploadId: String,
        @Query("offset") offset: Int,
        @Part fileData: MultipartBody.Part
    ): ResponseWrapper<SendPartialUploadDataResponseDto>

    @GET("/v2/uploads/partial/{id}/attachment")
    suspend fun getChatAttachmentPartialUpload(
        @Path("id") partialUploadId: String,
        @Query("source") source: String
    ): ResponseWrapper<ChatAttachmentPartialUploadDto>


}
