package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

@Deprecated("transition to auth module")
data class Authenticate(

        @SerializedName("code_authentication")
        val codeAuthentication: String,

        @SerializedName("errors")
        val errors: List<ErrorAuthenticate>
)

// Get token
@Deprecated("transition to auth module")
data class GetToken(

        @SerializedName("access_token")
        val accessToken: String,

        @SerializedName("expires_in")
        val expiresIn: String,

        @SerializedName("refresh_token")
        val refreshToken: String,

        @SerializedName("token_type")
        val tokenType: String,

        @SerializedName("errors")
        val errors: List<ErrorAuthenticate>
)

// Errors ---------------------
@Deprecated("transition to auth module")
data class ErrorAuthenticate(

        @SerializedName("field")
        val field: String,

        @SerializedName("reason")
        val reason: String
)
