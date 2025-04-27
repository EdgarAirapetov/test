package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/*
 * created by c7j on 28.08.18
 */
data class OldUser(
        @SerializedName("login") var login: String?,
        @SerializedName("account_type") var accountType: Int = 0,
        @SerializedName("account_color") var accountColor: Int = 0,
        @SerializedName("avatar") var ava: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("number") var number: String?,
        @SerializedName("gender") var gender: Int = 0,
        @SerializedName("user_id") var userId: Long?,
        @SerializedName("age") var age: Int?,
        @SerializedName("city") var city: String?


) : Serializable {
    override fun toString(): String {
        return "OldUser(login=$login, accountType=$accountType, color=$accountColor, ava=$ava," +
            "number=$number, name=$name gender=$gender, userId=$userId, age=$age, city=$city)"
    }
}
