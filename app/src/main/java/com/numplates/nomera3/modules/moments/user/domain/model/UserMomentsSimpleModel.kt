package com.numplates.nomera3.modules.moments.user.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMomentsSimpleModel(
    val hasMoments: Boolean,
    val hasNewMoments: Boolean
) : Parcelable
