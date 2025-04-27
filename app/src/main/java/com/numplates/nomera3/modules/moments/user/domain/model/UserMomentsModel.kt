package com.numplates.nomera3.modules.moments.user.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMomentsModel(
    val hasMoments: Boolean,
    val hasNewMoments: Boolean,
    val countNew: Int,
    val countTotal: Int,
    val previews: List<UserMomentsPreviewModel>
) : Parcelable
