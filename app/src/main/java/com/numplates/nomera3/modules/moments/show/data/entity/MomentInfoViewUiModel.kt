package com.numplates.nomera3.modules.moments.show.data.entity

import android.os.Parcelable
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentInfoViewUiModel(
    val momentGroups: List<MomentGroupUiModel>
): Parcelable
