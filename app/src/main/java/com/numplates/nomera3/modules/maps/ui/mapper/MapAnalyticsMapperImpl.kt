package com.numplates.nomera3.modules.maps.ui.mapper

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDayWeekEvent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventDeletedEventParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MapAnalyticsMapperImpl @Inject constructor() : MapAnalyticsMapper {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun mapAmplitudePropertyMapEventsTypeEvent(eventType: EventType): AmplitudePropertyMapEventsTypeEvent =
        when (eventType) {
            EventType.EDUCATION -> AmplitudePropertyMapEventsTypeEvent.EDUCATION
            EventType.ART -> AmplitudePropertyMapEventsTypeEvent.ART
            EventType.CONCERT -> AmplitudePropertyMapEventsTypeEvent.CONCERT
            EventType.SPORT -> AmplitudePropertyMapEventsTypeEvent.SPORT
            EventType.TOURISM -> AmplitudePropertyMapEventsTypeEvent.TOURISM
            EventType.GAMES -> AmplitudePropertyMapEventsTypeEvent.GAMES
            EventType.PARTY -> AmplitudePropertyMapEventsTypeEvent.PARTY
        }

    override fun mapMapEventInvolvementParamsAnalyticsModel(
        post: PostUIEntity
    ): MapEventInvolvementParamsAnalyticsModel? {
        return MapEventInvolvementParamsAnalyticsModel(
            membersCount = post.event?.participation?.participantsCount ?: return null,
            reactionCount = post.reactions?.size ?: 0,
            commentCount = post.commentCount
        )
    }

    override fun mapMapEventDeletedEventParamsAnalyticsModel(
        postUIEntity: PostUIEntity
    ): MapEventDeletedEventParamsAnalyticsModel? {
        val event = postUIEntity.event ?: return null
        val user = postUIEntity.user ?: return null
        val mapEventInvolvementParamsAnalyticsModel = mapMapEventInvolvementParamsAnalyticsModel(postUIEntity)
            ?: return null
        val zoneId = ZoneId.of(event.timeZoneId)
        val eventZonedDateTime = Instant.ofEpochMilli(event.timestampMs).atZone(zoneId)
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = timeFormatter.format(time)
        val dateString = dateFormatter.format(date)
        val typeEvent = mapAmplitudePropertyMapEventsTypeEvent(event.eventType)
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = event.id,
            authorId = user.userId
        )
        val eventTimer = maxOf(
            Duration.ofMillis(event.timestampMs - System.currentTimeMillis()).toHours(),
            0
        ).toInt()
        return MapEventDeletedEventParamsAnalyticsModel(
            dateEvent = dateString,
            timeEvent = timeString,
            typeEvent = typeEvent,
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel,
            eventTimer = eventTimer
        )
    }

    override fun mapMapEventCreatedEventParamsAnalyticsModel(
        postUIEntity: PostUIEntity
    ): MapEventCreatedEventParamsAnalyticsModel? {
        val event = postUIEntity.event ?: return null
        val user = postUIEntity.user ?: return null
        val zoneId = ZoneId.of(event.timeZoneId)
        val eventZonedDateTime = Instant.ofEpochMilli(event.timestampMs).atZone(zoneId)
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = timeFormatter.format(time)
        val dateString = dateFormatter.format(date)
        val charDescriptionCount = postUIEntity.tagSpan?.text?.length ?: 0
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = event.id,
            authorId = user.userId
        )
        val eventType = mapAmplitudePropertyMapEventsTypeEvent(event.eventType)
        val dayWeekEvent = mapAmplitudePropertyMapEventsDayWeekEvent(date.dayOfWeek)
        val address = "${event.address.name}, ${event.address.addressString}"
        return MapEventCreatedEventParamsAnalyticsModel(
            eventDate = dateString,
            eventTime = timeString,
            eventType = eventType,
            havePhoto = postUIEntity.getImageUrl() != null,
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            charDescriptionCount = charDescriptionCount,
            eventName = event.title,
            eventLocation = address,
            dayWeekEvent = dayWeekEvent
        )
    }

    private fun mapAmplitudePropertyMapEventsDayWeekEvent(
        dayOfWeek: DayOfWeek
    ): AmplitudePropertyMapEventsDayWeekEvent =
        when (dayOfWeek) {
            DayOfWeek.MONDAY -> AmplitudePropertyMapEventsDayWeekEvent.MONDAY
            DayOfWeek.TUESDAY -> AmplitudePropertyMapEventsDayWeekEvent.TUESDAY
            DayOfWeek.WEDNESDAY -> AmplitudePropertyMapEventsDayWeekEvent.WEDNESDAY
            DayOfWeek.THURSDAY -> AmplitudePropertyMapEventsDayWeekEvent.THURSDAY
            DayOfWeek.FRIDAY -> AmplitudePropertyMapEventsDayWeekEvent.FRIDAY
            DayOfWeek.SATURDAY -> AmplitudePropertyMapEventsDayWeekEvent.SATURDAY
            DayOfWeek.SUNDAY -> AmplitudePropertyMapEventsDayWeekEvent.SUNDAY
        }

}
