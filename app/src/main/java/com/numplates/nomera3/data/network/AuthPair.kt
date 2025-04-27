package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthPair(
        @SerializedName("login") var login: String?,
        @SerializedName("password") var password: String?
): Serializable {

    override fun toString(): String {
        return "AuthPair(login=$login, password=$password)"
    }
}