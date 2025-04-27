package com.numplates.nomera3.modules.moments.comments.data

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MomentsCommentApi {
    @GET("/v2/users/moment/{moment_id}/comments")
    suspend fun getComments(
        @Path("moment_id") momentItemId: Long,
        @Query("limit") limit: Long,
        @Query("start_id") startId: Long? = null,
        @Query("parent_id") parentId: Long? = null,
        @Query("comment_id") commentId: Long? = null,
        @Query("order") order: Int? = null
    ): ResponseWrapper<CommentsEntityResponse?>

    @POST("/v2/users/moment/comment")
    suspend fun sendComment(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<SendCommentResponse>

    @DELETE("/v2/users/moment/comment/{comment_id}")
    suspend fun deleteComment(@Path("comment_id") commentId: Long): ResponseWrapper<Any>
}
