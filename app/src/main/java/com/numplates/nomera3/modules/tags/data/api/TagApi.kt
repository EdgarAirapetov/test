package com.numplates.nomera3.modules.tags.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.data.entity.UniqueNameTagListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TagApi {

    @GET("/v2/users/search/uniqname")
    suspend fun getTagListByUniqueName(
        @Query("query") query: String,
        @Query("room_id") roomId: Int?
    ): ResponseWrapper<UniqueNameTagListResponse>

    /**
     * https://nomera.atlassian.net/wiki/spaces/NOMIT/pages/2339897373/Search
     * */
    @GET("/v2/search/hashtags")
    suspend fun getTagListByHashtag(
        @Query("query") query: String?,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("is_search_screen") isSearchScreen: Int = 1
    ): ResponseWrapper<HashtagTagListModel>
}