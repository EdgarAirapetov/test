package com.numplates.nomera3.modules.maps.domain.model

import android.os.Parcelable
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserPinModel(
    val uid: Long,
    val name: String?,
    val uniqueName: String?,
    val avatar: String?,
    val gender: Gender?,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val isFriend: Boolean,
    val coordinates: CoordinatesModel,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean,
) : Parcelable
