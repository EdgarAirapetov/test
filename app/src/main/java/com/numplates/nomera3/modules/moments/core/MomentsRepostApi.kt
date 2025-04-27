package com.numplates.nomera3.modules.moments.core

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.moments.show.data.MomentLinkResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MomentsRepostApi {
    @FormUrlEncoded
    @POST("/v2/users/moment/{moment_id}/repost")
    suspend fun shareMoment(
        @Path("moment_id") momentId: Long,
        @Field("comment") comment: String,
        @Field("user_ids[]") userIds: List<Long>,
        @Field("room_ids[]") roomIds: List<Long>
    ): ResponseWrapper<MomentItemDto>

    @GET("/v2/users/moment/{moment_id}/get_link")
    suspend fun getMomentLink(
        @Path("moment_id") momentId: Long
    ): ResponseWrapper<MomentLinkResponseDto>
}
