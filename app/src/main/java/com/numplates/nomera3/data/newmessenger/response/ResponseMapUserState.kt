package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName

data class ResponseMapUserState(

        @SerializedName("id")
        val id: Long,

        @SerializedName("lat")
        val lat: Float,

        @SerializedName("lon")
        val lon: Float,

        @SerializedName("state_id")
        val stateId: Int,

        @SerializedName("state_duration")
        val stateDuration: Int,

        @SerializedName("state_time")
        val stateTime: Long

)