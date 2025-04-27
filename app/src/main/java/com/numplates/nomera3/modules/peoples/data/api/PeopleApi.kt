package com.numplates.nomera3.modules.peoples.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.peoples.data.dto.ApprovedUsersDto
import com.numplates.nomera3.modules.peoples.data.dto.RelatedUsersDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PeopleApi {

    @GET("v2/users/search/top_users")
    suspend fun getTopUsers(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ) : ResponseWrapper<List<ApprovedUsersDto>>

    @GET("/v2/users/search/related")
    suspend fun getRelatedUsers(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("selected_user_id") selectedUserId: Long? = null
    ) : ResponseWrapper<List<RelatedUsersDto>>
}
