package com.numplates.nomera3.modules.user.data.entity

import com.google.gson.annotations.SerializedName

data class UserPhone(

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    val isHidden: Boolean = false

)