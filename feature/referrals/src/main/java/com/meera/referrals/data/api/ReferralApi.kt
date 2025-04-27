package com.meera.referrals.data.api

import com.meera.core.network.ResponseWrapper
import com.meera.referrals.data.model.ReferralDataDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ReferralApi {

    @GET("/v2/users/referal")
    suspend fun getReferral(): ResponseWrapper<ReferralDataDto>

    @PATCH("/v2/users/referal/vip")
    suspend fun getVipReferral(): Response<Any>

    @GET("/v2/users/referal/check")
    suspend fun checkReferralCode(@Query("code") code: String): ResponseWrapper<Any>

    @FormUrlEncoded
    @POST("/v2/users/referal")
    suspend fun registerReferralCode(@Field("code") code: String): Response<Any>

}
