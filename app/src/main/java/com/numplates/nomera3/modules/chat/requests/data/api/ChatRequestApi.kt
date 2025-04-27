package com.numplates.nomera3.modules.chat.requests.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.data.newmessenger.response.Dialog
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatRequestApi {

    @FormUrlEncoded
    @POST("/v2/rooms/{id}/approved")
    suspend fun chatRequestAvailability(
        @Path("id") postId: Long,
        @Field("approved") approved: Int
    ): ResponseWrapper<Dialog?>

}
