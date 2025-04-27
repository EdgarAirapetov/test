package com.numplates.nomera3.modules.moments.show.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentLinkModel(
    val deepLinkUrl: String
): Parcelable
