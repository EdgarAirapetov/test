package com.numplates.nomera3.modules.maps.ui.events.model

import android.os.Parcelable
import com.meera.db.models.message.ParsedUniquename
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.ParticipationModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventUiModel(
    val id: Long,
    val title: String,
    val tagSpan: ParsedUniquename?,
    val address: AddressUiModel,
    val timestampMs: Long,
    val timeZoneId: String,
    val eventType: EventType,
    val participantAvatars: List<String?>,
    val participation: ParticipationModel,
) : Parcelable
