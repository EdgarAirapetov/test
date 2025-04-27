package com.numplates.nomera3.modules.maps.domain.analytics.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent

data class MapEventDeletedEventParamsAnalyticsModel(
    val dateEvent: String,
    val timeEvent: String,
    val typeEvent: AmplitudePropertyMapEventsTypeEvent,
    val mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
    val mapEventInvolvementParamsAnalyticsModel: MapEventInvolvementParamsAnalyticsModel,
    val eventTimer: Int
)
