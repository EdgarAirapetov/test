package com.numplates.nomera3.modules.user.data.api

import com.numplates.nomera3.data.network.BlockRequest
import com.numplates.nomera3.data.network.BlockSuggestionDto
import com.numplates.nomera3.data.network.ListFriendsResponse
import com.numplates.nomera3.data.network.ListMutualUsersDto
import com.numplates.nomera3.data.network.ListSubscriptionResponse
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPermissionResponse
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.modules.user.data.model.DeleteAvatarItemRequest
import com.numplates.nomera3.modules.user.data.model.UploadAvatarIdRequest
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @DELETE("/v1/users/{id}/remove_friend")
    suspend fun removeUserAndSaveSubscription(
        @Path("id") userId: Long,
        @Query("type") userType: String
    ): Response<Unit>

    @DELETE("/v1/users/{id}/remove_friend")
    suspend fun removeUserAndCancelSubscription(
        @Path("id") userId: Long
    ): Response<Unit>

    @POST("/v2/users/subscription")
    suspend fun subscribeUser(
        @Body params: HashMap<String, Any>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/v2/users/subscription", hasBody = true)
    suspend fun unsubscribeUser(
        @Body params: HashMap<String, Any>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/v2/users/subscribers", hasBody = true)
    suspend fun removeSubscriber(
        @Body params: HashMap<String, Any>
    ): Response<Unit>

    @FormUrlEncoded
    @POST("/v2/users/message/blacklist")
    suspend fun addPrivateMessageUserToBlackList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @FormUrlEncoded
    @POST("/v2/users/message/whitelist")
    suspend fun addPrivateMessageUserToWhiteList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @POST("/v2/users/profile/statistics/as_read")
    suspend fun setProfileStatisticsAsRead(): ResponseWrapper<Any>

    /**
     * Добавить юзера в черный список и убрать юзера из черного списка
     * */
    @POST("/v1/users/{id}/blacklist")
    suspend fun setBlockedStatusToUser(
        @Path("id") currentUserId: Long,
        @Body blockRequest: BlockRequest?
    ): ResponseWrapper<Boolean?>

    /**
     * Добавить аватар
     */
    @Multipart
    @POST("/v2/uploads/images/users/avatars")
    fun uploadAvatar(
        @Part imageFile: MultipartBody.Part?,
        @Part("avatar_animation") avatarAnimation: RequestBody?
    ): Single<ResponseWrapper<UploadAvatarResponse?>>

    @POST("/v2/uploads/images/users/avatars")
    suspend fun uploadAvatarWithUploadId(
        @Body uploadAvatarIdRequest: UploadAvatarIdRequest
    ): ResponseWrapper<UploadAvatarResponse>

    /**
     * Удалить аватар
     */
    @DELETE("/v1/users/{user_id}/avatar")
    fun deleteUserAvatar(
        @Path("user_id") userID: Long
    ): Single<ResponseWrapper<UploadAvatarResponse?>>

    @GET("/v2/users/permissions")
    suspend fun requestUserPermissions(): ResponseWrapper<UserPermissionResponse>

    @DELETE("/v2/users/galleries/{galleryItemId}")
    suspend fun deleteGalleryItem(@Path("galleryItemId") galleryItemId: Long)

    @HTTP(method = "DELETE", path = "/v3/users/avatars/delete_avatar", hasBody = true)
    suspend fun deleteAvatarItem(@Body deleteAvatarItemRequest: DeleteAvatarItemRequest)

    /**
     * Получить список подписок юзера
     * */
    @GET("/v2/users/{user_id}/subscription")
    suspend fun getUserSubscriptions(
        @Path("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("query") querySearch: String?
    ): ResponseWrapper<ListSubscriptionResponse>

    @GET("/v2/users/{user_id}/friends")
    suspend fun getUserFriends(
        @Path("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("query") querySearch: String?
    ): ResponseWrapper<ListFriendsResponse>

    @GET("/v2/users/{user_id}/subscribers")
    suspend fun getUserSubscribers(
        @Path("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("query") querySearch: String?
    ): ResponseWrapper<ListSubscriptionResponse>

    @GET("/v2/users/{user_id}/mutual")
    suspend fun getUserMutual(
        @Path("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("query") querySearch: String?
    ): ResponseWrapper<ListMutualUsersDto>

    /**
     * Поиск по подписчкам
     * */
    @GET("/v2/users/subscription/search")
    suspend fun subscriptionsSearch(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String,
        @Query("name") name: String
    ): ResponseWrapper<ListSubscriptionResponse>

    @GET("/v2/users/phone_number")
    suspend fun getOwnUserPhone(): ResponseWrapper<UserPhone>

    @GET("/v2/users/email")
    suspend fun getOwnUserEmail(): ResponseWrapper<UserEmail>

    /**
     * Устанавливается флаг о том, что юзер посмотрел поздравление с Днем Рождения
     */
    @POST("/v1/info/birthday_congratulation_viewed")
    suspend fun uploadBirthdayViewed(): ResponseWrapper<Any>

    @POST("/v3/users/block_suggestion")
    suspend fun blockSuggestion(
        @Body model: BlockSuggestionDto
    ) : ResponseWrapper<Any>
}
