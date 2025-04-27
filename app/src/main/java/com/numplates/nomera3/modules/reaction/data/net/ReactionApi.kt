package com.numplates.nomera3.modules.reaction.data.net

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReactionApi {
    @POST("/v2/users/moment/comment/{comment_id}/add_reaction")
    suspend fun addMomentCommentReaction(
        @Path("comment_id") commentId: Long,
        @Query("reaction") reaction: String,
    ): ResponseWrapper<CommentEntityResponse>

    @DELETE("/v2/users/moment/comment/{comment_id}/remove_reaction")
    suspend fun removeMomentCommentReaction(
        @Path("comment_id") commentId: Long
    ): ResponseWrapper<CommentEntityResponse>

    @POST("/v3/comments/{comment_id}/add_reaction")
    suspend fun addCommentReaction(
        @Path("comment_id") commentId: Long,
        @Query("reaction") reaction: String
    ): ResponseWrapper<ReactionApiCommentResponse>

    @DELETE("/v3/comments/{comment_id}/remove_reaction")
    suspend fun removeCommentReaction(
        @Path("comment_id") commentId: Long
    ): ResponseWrapper<ReactionApiCommentResponse>

    @POST("/v3/posts/{post_id}/add_reaction")
    suspend fun addPostReaction(
        @Path("post_id") postId: Long,
        @Query("reaction") reaction: String
    ): ResponseWrapper<ReactionApiPostResponse?>

    @DELETE("/v3/posts/{post_id}/remove_reaction")
    suspend fun removePostReaction(
        @Path("post_id") postId: Long
    ): ResponseWrapper<ReactionApiPostResponse?>

    @POST("/v2/users/moment/{moment_id}/add_reaction")
    suspend fun addMomentReaction(
        @Path("moment_id") momentId: Long,
        @Query("reaction") reaction: String
    ): ResponseWrapper<ReactionApiMomentResponse>

    @DELETE("/v2/users/moment/{moment_id}/remove_reaction")
    suspend fun removeMomentReaction(
        @Path("moment_id") momentId: Long
    ): ResponseWrapper<ReactionApiMomentResponse>
}
