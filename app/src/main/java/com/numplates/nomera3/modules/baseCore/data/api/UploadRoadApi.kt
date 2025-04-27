package com.numplates.nomera3.modules.baseCore.data.api

import com.numplates.nomera3.data.network.PostCreationResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

//Используется для загрузок файлов, относящихся к дороге (Посты, моменты)
interface UploadRoadApi {

    @Multipart
    @POST("/v2/posts/")
    suspend fun addPost(
        @Part("group_id") groupId: RequestBody? = null,
        @Part("text") text: RequestBody? = null,
        @Part image: MultipartBody.Part? = null,
        @Part("upload_id") uploadId: RequestBody? = null,
        @Part uploadIds: List<MultipartBody.Part>? = null,
        @Part("privacy") roadType: RequestBody? = null,                            //фактически выбираем тип дороги // моя общая
        @Part("comment_availability") commentSetting: RequestBody? = null,         // кто может комментировать пост
        @Part("media") media: MediaEntity? = null,
        @Part("event") event: EventEntity? = null,
        @Part("background_id") backgroundId: RequestBody? = null,
        @Part("font_size") fontSize: RequestBody? = null,
        @Part("media_positioning") mediaPositioning: RequestBody? = null,
        @Part("upload_media_positioning") mediaPositioningList: RequestBody? = null
    ): ResponseWrapper<PostCreationResponse?>?

    @Multipart
    @POST("/v2/rooms/change_avatar")
    suspend fun uploadGroupChatAvatar(
            @Query("target_id") roomId: Long,
            @Part imageFile: MultipartBody.Part?
    ): ResponseWrapper<Any>
}
