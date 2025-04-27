package com.numplates.nomera3.modules.user.data.entity

import com.google.gson.annotations.SerializedName

data class UserEmail(

    @SerializedName("email")
    val email: String? = null,

    val isHidden: Boolean = false

)