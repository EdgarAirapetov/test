package com.numplates.nomera3.modules.reactionStatistics.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.reactionStatistics.data.entity.ReactionsRootDto
import com.numplates.nomera3.modules.reactionStatistics.data.entity.viewers.ViewersRootDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReactionsApi {

    @GET("/v3/posts/reactions")
    suspend fun getPostReactions(
        @Query("post_id") postId: Long,
        @Query("reaction") reaction: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<List<ReactionsRootDto>>

    @GET("/v3/comments/reactions")
    suspend fun getCommentReactions(
        @Query("comment_id") commentId: Long,
        @Query("reaction") reaction: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<List<ReactionsRootDto>>

    @GET("/v2/users/moment/{moment_id}/reactions")
    suspend fun getMomentReactions(
        @Path("moment_id") momentId: Long,
        @Query("reaction") reaction: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<List<ReactionsRootDto>>

    @GET("/v2/users/moment/{moment_id}/viewers")
    suspend fun getMomentViewers(
        @Path("moment_id") momentId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<ViewersRootDto>

    @GET("/v2/users/moment/comment/{comment_id}/reactions")
    suspend fun getMomentCommentReactions(
        @Path("comment_id") commentId: Long,
        @Query("reaction") reaction: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<List<ReactionsRootDto>>
}
