package com.numplates.nomera3.modules.search.data.api

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.search.data.entity.RecentGroupEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentHashtagEntityResponse
import com.numplates.nomera3.modules.search.data.entity.RecentSearchResult
import com.numplates.nomera3.modules.search.data.entity.RecentUserEntityResponse
import com.numplates.nomera3.modules.search.data.entity.SearchGroupEntityResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface SearchApi {

    suspend fun searchAll()     // TODO: 20.05.2021 Пока не делаем !

    @GET("/groups/subscribe_group")
    suspend fun subscribeGroup(
        @Query("group_id") groupId: Int
    ): ResponseWrapper<List<*>>

    /**
     * - friendsType: null для простого поиска по всем пользователям
     * - userType: UserSimple
     */
    @GET("v1/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("friends") friendsType: Any?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String,
        @Query("gender") gender: Int?,
        @Query("age_from") ageFrom: Int?,
        @Query("age_to") ageTo: Int?,
        @Query("cities") cityIds: String?,
        @Query("countries") countryIds: String?,
    ): ResponseWrapper<List<UserSimple>>

    @GET("v1/users/searchbynumber")
    suspend fun searchUsersByNumber(
        @Query("number") number: String,
        @Query("country_id") countryId: Int,
        @Query("type_id") typeId: Int,
        @Query("user_type") userType: String
    ): ResponseWrapper<List<UserSimple>>

    /**
     * - quantity: limit
     * - group_type: 0 для простого поиска по сообществам
     */
    @GET("/groups/search_groups")
    suspend fun searchGroups(
        @Query("search") query: String,
        @Query("start_index") startIndex: Int,
        @Query("quantity") quantity: Int,
        @Query("group_type") groupType: Int
    ): ResponseWrapper<SearchGroupEntityResponse>

    @GET("/v2/history/user")
    suspend fun recentSearchUsers(): RecentSearchResult<RecentUserEntityResponse>

    @GET("/v2/history/group")
    suspend fun recentSearchGroups(): RecentSearchResult<RecentGroupEntityResponse>

    @GET("/v2/history/hashtag")
    suspend fun recentSearchHashtags(): RecentSearchResult<RecentHashtagEntityResponse>

    /**
     * Удаление недавних в поиске
     * - type (user | group | hashtag)
     */
    @DELETE("/v2/history/{type}")
    suspend fun clearSearchRecent(@Path("type") type: String): Response<Unit>

}
