package com.numplates.nomera3.presentation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VipStatus(
        var accountType: Int?,
        var accountColor: Int?
) : Parcelable