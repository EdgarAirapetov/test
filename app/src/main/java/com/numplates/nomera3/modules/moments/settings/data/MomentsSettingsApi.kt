package com.numplates.nomera3.modules.moments.settings.data

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface MomentsSettingsApi {
    @GET("/v2/users/privacy/moment/exclusion/hide_from")
    suspend fun getMomentsHideFromExclusionsWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @GET("/v2/users/privacy/moment/exclusion/hide_from/search")
    suspend fun searchMomentHideFromExclusion(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/moment/exclusion/hide_from")
    suspend fun addMomentHideFromExclusion(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/moment/exclusion/hide_from", hasBody = true)
    suspend fun deleteMomentHideFromExclusion(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/moment/exclusion/not_show")
    suspend fun getMomentsNotShowExclusionsWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @GET("/v2/users/privacy/moment/exclusion/not_show/search")
    suspend fun searchMomentNotShowExclusion(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/moment/exclusion/not_show")
    suspend fun addMomentNotShowExclusion(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/moment/exclusion/not_show", hasBody = true)
    suspend fun deleteMomentNotShowExclusion(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>
}
