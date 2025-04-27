package com.numplates.nomera3.modules.maps.ui.events.navigation.model

import android.os.Parcelable
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventNavigationInitUiModel(
    val event: EventUiModel,
    val authorId: Long
) : Parcelable
