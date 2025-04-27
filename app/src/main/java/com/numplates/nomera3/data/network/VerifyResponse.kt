package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class VerifyResponse(

        @SerializedName("status")
        val isVerified: Boolean
)