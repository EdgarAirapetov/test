package com.numplates.nomera3.modules.reactionStatistics.domain.models

data class UserModel(
    val accountColor: Int,
    val accountType: Int,
    val approved: Int,
    val avatar: String,
    val birthday: Long,
    val city: String?,
    val gender: Int,
    val id: Long,
    val name: String?,
    val topContentMaker: Int,
    val uniqname: String?,
)
