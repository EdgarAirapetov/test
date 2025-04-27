package com.numplates.nomera3.modules.auth.data.entity

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName

data class Token(

    @SerializedName("access_token")
    val accessToken: String?,

    @SerializedName("expires_in")
    val expiresIn: String?,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("token_type")
    val tokenType: String?,

    @Nullable
    @SerializedName("errors")
    val errors: List<AuthenticationError>
)
