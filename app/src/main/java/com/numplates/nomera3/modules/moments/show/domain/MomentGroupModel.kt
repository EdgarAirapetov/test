package com.numplates.nomera3.modules.moments.show.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentGroupModel(
    val isMine: Boolean,
    val moments: List<MomentItemModel>,
    val userId: Long
) : Parcelable
