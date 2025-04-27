package com.numplates.nomera3.modules.userprofile.domain.model.usermain

data class MutualUsersModel(
    val userIds: List<Int>? = null,
    val userSimple: List<UserSimpleModel>? = null,
    val moreCount: Int? = 0
)
