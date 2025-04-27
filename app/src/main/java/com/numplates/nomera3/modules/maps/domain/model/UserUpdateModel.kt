package com.numplates.nomera3.modules.maps.domain.model

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import java.util.Date

data class UserUpdateModel(
    val uid: Long,
    val name: String?,
    val uniqueName: String?,
    val birthday: Date?,
    val avatar: String?,
    val avatarBig: String?,
    val gender: Gender?,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val city: String?,
    val country: String?,
    val approved: Boolean,
    val friendStatus: Int,
    val subscriptionOn: Boolean,
    val subscribersCount: Long,
    val profileBlocked: Boolean,
    val profileDeleted: Boolean,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean
)
