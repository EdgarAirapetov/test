package com.numplates.nomera3.modules.moments.show.presentation.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentGroupUiModel(
    val id: Long,
    val moments: List<MomentItemUiModel>,
    val userId: Long,
    val placeholder: String?,
    val isMine: Boolean
): Parcelable {

    val firstNotViewedMomentPreview: String?
        get() = moments.sortedBy { it.createdAt }.find { !it.isViewed } ?.contentPreview

    val firstNotViewedMomentId: Long?
        get() = moments.sortedBy { it.createdAt }.find { !it.isViewed } ?.id

    val lastMomentId: Long?
        get() = moments.maxByOrNull { it.createdAt }?.id

    val latestMomentPreview: String
        get() = moments.maxByOrNull { it.createdAt } ?.contentPreview ?: ""

    val latestCreatedAt: Long
        get() = moments.maxByOrNull { it.createdAt } ?.createdAt ?: Long.MIN_VALUE

    val isViewed: Boolean
        get() = moments.all { it.isViewed }

    val momentsCount : Int
        get() = moments.size
}
