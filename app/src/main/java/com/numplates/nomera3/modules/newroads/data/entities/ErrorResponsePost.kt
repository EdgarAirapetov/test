package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.annotations.SerializedName

data class ErrorResponsePost (
    @SerializedName("errors")
    val errors: List<ErrorsCreatePost?>? = null
)

data class ErrorsCreatePost(
    @SerializedName("field")
    val field: String? = null,

    @SerializedName("reason")
    val reason: String? = null
)