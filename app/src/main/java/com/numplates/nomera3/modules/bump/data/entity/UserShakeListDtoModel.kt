package com.numplates.nomera3.modules.bump.data.entity

import com.google.gson.annotations.SerializedName

data class UserShakeListDtoModel(
    @SerializedName("users")
    val userShakeList: List<UserShakeDtoModel>?
)
