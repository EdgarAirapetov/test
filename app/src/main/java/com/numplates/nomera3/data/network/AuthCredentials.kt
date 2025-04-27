package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthCredentials(
        @SerializedName("email") var email: String?,
        @SerializedName("phone_number") var phoneNumber: String?,
        @SerializedName("code") var code:String?,
        @SerializedName("device") var device: String,
        @SerializedName("udid") var udid: String,
        @SerializedName("old_token") var oldToken: String?,
        @SerializedName("old_login") var oldLogin: String?,
        @SerializedName("device_token") var deviceToken: String?,
        @SerializedName("has_empty_profile") var hasEmptyProfile: Boolean?,
        @SerializedName("twins") var twins: List<OldUser?>?,
        @SerializedName("login") var login: String?,
        @SerializedName("password") var password: String?


): Serializable {

    override fun toString(): String {
        return "AuthCredentials(email=$email, phoneNumber=$phoneNumber, code=$code, " +
            "device='$device', udid='$udid', oldToken=$oldToken, oldLogin=$oldLogin, " +
            "deviceToken=$deviceToken, hasEmptyProfile=$hasEmptyProfile, twins=$twins)"
    }
}
