package com.numplates.nomera3.modules.maps.domain.analytics.model

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDayWeekEvent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent

data class MapEventCreatedEventParamsAnalyticsModel(
    val eventDate: String,
    val eventTime: String,
    val eventType: AmplitudePropertyMapEventsTypeEvent,
    val havePhoto: Boolean,
    val mapEventIdParamsAnalyticsModel: MapEventIdParamsAnalyticsModel,
    val charDescriptionCount: Int,
    val eventName: String,
    val eventLocation: String,
    val dayWeekEvent: AmplitudePropertyMapEventsDayWeekEvent
)
