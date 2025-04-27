package com.numplates.nomera3.modules.moments.show.data.entity

import android.os.Parcelable
import com.numplates.nomera3.modules.moments.show.domain.MomentGroupModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentInfoModel(
    val momentGroups: List<MomentGroupModel>,
    val session: String? = null,
    val lastPageSize: Int = 0,
    val isPlaceType: Boolean = false // TODO (BR-13815) add proper logic when backend will be able to return Place cards
) : Parcelable
