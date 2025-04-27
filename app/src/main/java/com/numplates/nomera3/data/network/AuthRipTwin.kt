package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthRipTwin(
        @SerializedName("old_token") var oldToken: String?,
        @SerializedName("old_login") var oldLogin: String?
): Serializable {

    override fun toString(): String {
        return "AuthRipTwin(oldToken=$oldToken, oldLogin=$oldLogin)"
    }
}