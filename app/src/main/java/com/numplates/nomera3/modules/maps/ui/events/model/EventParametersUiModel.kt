package com.numplates.nomera3.modules.maps.ui.events.model

import android.os.Parcelable
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class EventParametersUiModel(
    val address: AddressUiModel,
    val date: LocalDate,
    val time: LocalTime,
    val timeZoneId: String,
    val eventType: EventType,
    val placeId: Long,
    val model: UIAttachmentPostModel? = null
) : Parcelable
