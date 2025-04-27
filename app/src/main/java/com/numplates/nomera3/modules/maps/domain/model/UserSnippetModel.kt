package com.numplates.nomera3.modules.maps.domain.model

import android.os.Parcelable
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class UserSnippetModel(
    val uid: Long,
    val name: String?,
    val uniqueName: String?,
    val birthday: Date?,
    val avatar: String?,
    val avatarBig: String?,
    val hatLink: String?,
    val gender: Gender?,
    val distance: Double,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val city: String?,
    val country: String?,
    val coordinates: CoordinatesModel,
    val approved: Boolean,
    val friendStatus: Int,
    val subscriptionOn: Boolean,
    val subscribersCount: Long,
    val profileBlocked: Boolean,
    val profileDeleted: Boolean,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean,
    val topContentMaker: Boolean,
    val moments: UserMomentsModel?
) : Parcelable
