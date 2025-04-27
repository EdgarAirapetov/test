package com.numplates.nomera3.modules.moments.show.data.entity

import android.os.Parcelable
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentInfoCarouselUiModel(
    val momentsCarouselList: List<MomentCarouselItem>? = null,
    val pagingTicket: String? = null,
    val useCreateMomentItem: Boolean = false
) : Parcelable
