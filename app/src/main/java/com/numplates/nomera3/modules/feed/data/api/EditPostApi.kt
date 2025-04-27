package com.numplates.nomera3.modules.feed.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface EditPostApi {

    @PATCH("/v3/posts/{id}/update_post")
    suspend fun updatePost(
        @Path("id") postId: Long,
        @Body body: HashMap<String, Any?>,
        @Query("user_type") userType: String
    ): ResponseWrapper<PostEntityResponse>
}
