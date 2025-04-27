package com.numplates.nomera3.modules.auth.data.api

import com.numplates.nomera3.modules.auth.data.entity.Authenticate
import com.numplates.nomera3.modules.auth.data.entity.Token
import com.numplates.nomera3.modules.auth.data.entity.SendCodeResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @FormUrlEncoded
    @POST("/v2/authentication/send_code")
    suspend fun sendCodeEmail(
        @Field("email") email: String
    ): Response<SendCodeResponse>

    @FormUrlEncoded
    @POST("/v3/authentication/send_code")
    suspend fun sendCodePhone(
        @Field("phone") phone: String
    ): Response<SendCodeResponse>

    @FormUrlEncoded
    @POST("/v2/authentication/authenticate")
    suspend fun authenticateEmail(
        @Field("email") email: String,
        @Field("code") code: String,
        @Field("code_challenge") codeChallenge: String,
        @Field("code_challenge_method") codeChallengeMethod: String
    ): Response<Authenticate>

    @FormUrlEncoded
    @POST("/v2/authentication/authenticate")
    suspend fun authenticatePhone(
        @Field("phone") phone: String,
        @Field("code") code: String,
        @Field("code_challenge") codeChallenge: String,
        @Field("code_challenge_method") codeChallengeMethod: String
    ): Response<Authenticate>

    @FormUrlEncoded
    @POST("/v2/authentication/token")
    suspend fun getAuthToken(
        @Field("code_authentication") authenticationCode: String,
        @Field("code_verifier") verifierCode: String
    ): Response<Token>
}
