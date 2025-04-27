package com.numplates.nomera3.modules.moments.user.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMomentsPreviewModel(
    val id: Long,
    val url: String,
    val viewed: Int
) : Parcelable
