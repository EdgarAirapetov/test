package com.numplates.nomera3.data.dbmodel

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
        @SerializedName("message")
        val message: String? = null
)