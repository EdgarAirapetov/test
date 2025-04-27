package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OnlineStatus(
        @SerializedName("online") var isOnline: Boolean,
        @SerializedName("last_active")  var lastActive: Long

)    :     Serializable

