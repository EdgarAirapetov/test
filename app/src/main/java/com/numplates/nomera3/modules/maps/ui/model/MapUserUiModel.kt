package com.numplates.nomera3.modules.maps.ui.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapUserUiModel(
    val id: Long,
    val name: String?,
    val uniqueName: String?,
    val avatar: String?,
    val hatLink: String?,
    val gender: Gender?,
    val accountType: AccountTypeEnum,
    val accountColor: Int?,
    val isFriend: Boolean,
    val latLng: LatLng,
    val blacklistedByMe: Boolean,
    val blacklistedMe: Boolean,
    val moments: UserMomentsModel?,
    val hasMoments: Boolean,
    val hasNewMoments: Boolean,
    val isFull: Boolean = false,
    val isShowOnMap: Boolean = true
) : Parcelable
