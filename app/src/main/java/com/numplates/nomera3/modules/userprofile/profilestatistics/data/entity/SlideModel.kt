package com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class SlidesListModel(
    val createdAt: Long,
    val slides: List<SlideModel>
)

@Parcelize
data class SlideModel(
    val type: String?,
    val count: Long?,
    val growth: Long?,
    val title: String,
    val text: String,
    val button: ButtonContentModel,
    val imageUrl: String?,
    val trend: ProfileStatisticsTrend
) : Parcelable

@Parcelize
data class ButtonContentModel(
    val text: String,
    val link: String?
) : Parcelable