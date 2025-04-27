package com.numplates.nomera3.data.dbmodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UsersWrapper<T>(

        @SerializedName("users")
        val users: List<T>

) : Serializable

data class UserWrapperWithCounter<T>(
        @SerializedName("count")
        var count: Long?,
        @SerializedName("users")
        val users: List<T>
)
