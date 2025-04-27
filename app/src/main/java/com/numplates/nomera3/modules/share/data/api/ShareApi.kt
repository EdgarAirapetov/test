package com.numplates.nomera3.modules.share.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.share.data.entity.LinkResponse
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ShareApi {

    /**
     * @param userId - id пользователя, профилем которого надо делиться
     */
    @FormUrlEncoded
    @POST("/v2/users/{user_id}/share/message")
    suspend fun shareUserProfile(
        @Path("user_id") userId: Long,
        @Field("comment") comment: String,
        @Field("user_ids[]") userIds: List<Long>,
        @Field("room_ids[]") roomIds: List<Long>
    ): ResponseWrapper<Any>

    /**
     * @param id - id сообщества для получения уникальной ссылки
     */
    @GET("/v2/groups/{id}/get_link")
    suspend fun getCommunityLink(
        @Path("id") groupId: Int
    ): ResponseWrapper<LinkResponse>

    /**
     * @param id - id сообщества, которым надо подклиться
     */
    @FormUrlEncoded
    @POST("/v2/groups/{id}/share/message")
    suspend fun shareCommunity(
        @Path("id") groupId: Int,
        @Field("comment") comment: String,
        @Field("user_ids[]") userIds: List<Long>,
        @Field("room_ids[]") roomIds: List<Long>,
    ): ResponseWrapper<Any>

    /**
     * @param postId - id поста для получения уникальной ссылки
     */
    @GET(" /v2/posts/{postId}/get_link")
    suspend fun getPostLink(
        @Path("postId") postId: Long
    ): ResponseWrapper<LinkResponse>

    @GET("/v2/share/contacts")
    suspend fun getShareItems(
        @Query("limit") limit: Int,
        @Query("query") query: String?,
        @Query("last_contact_id") lastId: String?,
        @Query("selected_user_id") selectedUserId: Long?,
    ): ResponseWrapper<List<ResponseShareItem>?>
}
