package com.meera.core.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ResponseWrapper<T> : Serializable {

    @SerializedName("success")
    var data: T? = null
        private set

    @SerializedName("error")
    var err: ResponseError? = null

    @SerializedName("code")
    var code = 0

    @SerializedName("message")
    var message: String? = null

    fun setData(data: T) {
        this.data = data
    }
}

data class ResponseError(
    @SerializedName("code") var code: Int,
    @SerializedName("message") var message: String?,
    @SerializedName("user_message") var userMessage: String?
) : Serializable
