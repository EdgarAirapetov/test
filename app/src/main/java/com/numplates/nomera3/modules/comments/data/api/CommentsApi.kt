package com.numplates.nomera3.modules.comments.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentsApi {

    @POST("/v2/complaints")
    suspend fun addComplainV2(
            @Body complainBody: HashMap<String, Any>
    ): ResponseWrapper<Any?>?

    @GET("/v3/posts/{post_id}/comments")
    suspend fun fetchComments(
        @Path("post_id") postId: Long,
        @Query("limit") limit: Long,
        @Query("start_id") startId: Long? = null,
        @Query("parent_id") parentId: Long? = null,
        @Query("comment_id")commentId: Long? = null,
        @Query("order") order: Int,
        @Query("sort_by") sortBy: Int
    ): ResponseWrapper<CommentsEntityResponse>

    @GET("/posts/delete_post_comment")
    suspend fun deletePostComment(@Query("comment_id") commentId: Long): ResponseWrapper<Any>

    @GET("/posts/delete_post_comment")
    suspend fun deleteComment(
            @Query("comment_id") commentId: Int
    ): ResponseWrapper<Any?>?


    @POST("/v3/posts/{post_id}/add_comment")
    suspend fun sendComment(
            @Path("post_id") postId: Long,
            @Body params: HashMap<String, Any>
    ): ResponseWrapper<SendCommentResponse?>?

    @GET("/v3/posts/{post_id}/comments?order=1&sort_by=0")
    suspend fun requestLastComment(
            @Path("post_id") postId: Long,
            @Query("limit") limit: Long,
    ): ResponseWrapper<CommentsEntityResponse?>
}

enum class OrderType(val value: Int) {
    INITIALIZE(0),
    AFTER(0),
    BEFORE(1)
}

enum class SortByType(val v0: Int) {
    TIMESTAMP(0),
    POPULARITY(1),
    LIKES(2),
    DISLIKES(3)
}
