package com.numplates.nomera3.modules.auth.data.entity

import com.google.gson.annotations.SerializedName

data class Authenticate(

    @SerializedName("code_authentication")
    val codeAuthentication: String?,

    @SerializedName("errors")
    val errors: List<AuthenticateErrors>?
)

data class AuthenticateErrors(
    @SerializedName("field")
    val field: String,

    @SerializedName("reason")
    val reason: String
)

data class AuthenticateError(

    @SerializedName("error")
    val error: String
)
