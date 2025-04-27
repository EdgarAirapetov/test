package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserStatus(
        @SerializedName("state_time") var stateTime: Long,
        @SerializedName("state_duration") var stateDuration: Int,
        @SerializedName("state_id") var stateId: Int,
        @SerializedName("state_counter") var stateCounter: Int
): Serializable