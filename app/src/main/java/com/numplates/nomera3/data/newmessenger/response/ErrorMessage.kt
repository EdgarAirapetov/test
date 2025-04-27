package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName

data class ErrorMessage(

        @SerializedName("error")
        val error: String,

        @SerializedName("status")
        val message: String?
)