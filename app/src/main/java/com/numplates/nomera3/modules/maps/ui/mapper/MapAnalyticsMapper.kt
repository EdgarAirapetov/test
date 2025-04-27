package com.numplates.nomera3.modules.maps.ui.mapper

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventDeletedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventType

interface MapAnalyticsMapper {
    fun mapAmplitudePropertyMapEventsTypeEvent(eventType: EventType): AmplitudePropertyMapEventsTypeEvent

    fun mapMapEventInvolvementParamsAnalyticsModel(
        post: PostUIEntity
    ): MapEventInvolvementParamsAnalyticsModel?

    fun mapMapEventDeletedEventParamsAnalyticsModel(
        postUIEntity: PostUIEntity
    ): MapEventDeletedEventParamsAnalyticsModel?

    fun mapMapEventCreatedEventParamsAnalyticsModel(
        postUIEntity: PostUIEntity
    ): MapEventCreatedEventParamsAnalyticsModel?
}
