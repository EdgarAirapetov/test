package com.numplates.nomera3.modules.userprofile.data.api

import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.userprofile.data.entity.AvatarDto
import com.numplates.nomera3.modules.userprofile.data.entity.ProfileSuggestionsDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserAvatarsDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserGalleryDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserProfileDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserProfileApi {
    @GET("/v2/users/{user_id}/profile?user_type=UserProfile")
    suspend fun getProfile(
        @Path("user_id") userId: Long, @Query("gps_x") gpsX: Float?, @Query("gps_y") gpsY: Float?
    ): ResponseWrapper<UserProfileDto>

    @GET("/v3/users/{user_id}/profile?user_type=UserProfile")
    suspend fun getProfileV3(
        @Path("user_id") userId: Long, @Query("gps_x") gpsX: Float?, @Query("gps_y") gpsY: Float?
    ): ResponseWrapper<UserProfileDto>

    @POST("/v3/users/{user_id}/set_viewed")
    suspend fun setProfileViewed(@Path("user_id") userId: Long): ResponseWrapper<EmptyModel>

    @GET("/v2/users/profile?user_type=UserProfile")
    suspend fun getOwnUserProfile(): ResponseWrapper<UserProfileDto>

    @PATCH("/v3/users/avatars/set_as_main_avatar")
    suspend fun setAvatarAsMain(@Query("id") photoId: Long): ResponseWrapper<AvatarDto>

    @GET("/v3/users/avatars/get_avatars")
    suspend fun getAvatars(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ResponseWrapper<UserAvatarsDto>

    @GET("/v2/users/galleries")
    suspend fun getGallery(
        @Query("id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ResponseWrapper<UserGalleryDto>

    @GET("v2/users/{userId}/suggested")
    suspend fun getProfileSuggestions(
        @Path("userId") userId: Long
    ): ResponseWrapper<ProfileSuggestionsDto>

    @POST("v1/users/{userId}/call_unavailable")
    suspend fun postCallUnavailable(
        @Path("userId") userId: Long
    ): ResponseWrapper<Boolean?>
}
