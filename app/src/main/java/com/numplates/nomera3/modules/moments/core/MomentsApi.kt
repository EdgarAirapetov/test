package com.numplates.nomera3.modules.moments.core

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.comments.data.MomentsCommentApi
import com.numplates.nomera3.modules.moments.settings.data.MomentsSettingsApi
import com.numplates.nomera3.modules.moments.show.data.GetMomentGroupsResponseDto
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.moments.show.data.AddMomentResponse
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntityData
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MomentsApi : MomentsSettingsApi, MomentsCommentApi, MomentsRepostApi {
    @Multipart
    @POST("/v2/users/moment/create_multiple")
    suspend fun addMoment(
        @Part("upload_ids[]") uploadIds: RequestBody,
        @Part("gps_x") gpsX: Double,
        @Part("gps_y") gpsY: Double,
        @Part("place") place:String? = null,
        @Part("data") data: Map<String, MediaEntityData>
    ): ResponseWrapper<AddMomentResponse?>

    @GET("v2/users/moment")
    suspend fun getMomentGroups(
        @Query("user_id") userId: Int,
        @Query("start_id") startId: Int,
        @Query("limit") limit: Int,
        @Query("type") type: String = "user",
        @Query("session_id") sessionId: String? = null,
        @Query("target_moment_id") targetMomentId: Long? = null,
    ): ResponseWrapper<GetMomentGroupsResponseDto>

    @DELETE("/v2/users/moment/{moment_id}")
    suspend fun deleteMoment(
        @Path("moment_id") momentId: Long
    ): ResponseWrapper<String>

    @POST("/v2/users/moment/{moment_id}/set_comment_availability")
    suspend fun setCommentAvailability(
        @Path("moment_id") momentId: Long,
        @Query("comment_availability") commentAvailability: Int
    )

    @GET("/v2/users/moment/{moment_id}")
    suspend fun getMomentById(
        @Path("moment_id") momentId: Long
    ): ResponseWrapper<MomentItemDto>

    @POST("/v2/complaints")
    suspend fun addComplainV2(
        @Body complainBody: HashMap<String, Any>
    ): ResponseWrapper<Any?>?

    @POST("/v2/users/moment/{moment_id}/set_viewed")
    suspend fun setMomentViewed(
        @Path("moment_id") momentId: Long
    ): ResponseWrapper<Any>
}
