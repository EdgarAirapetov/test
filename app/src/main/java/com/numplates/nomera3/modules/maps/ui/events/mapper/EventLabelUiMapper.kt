package com.numplates.nomera3.modules.maps.ui.events.mapper

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.domain.events.model.EventStatus
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventStatusUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class EventLabelUiMapper @Inject constructor(private val commonUiMapper: EventsCommonUiMapper) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun mapEventLabelUiModel(eventEntity: EventEntity, isVip: Boolean): EventLabelUiModel {
        val eventZonedDateTime = Instant.parse(eventEntity.date).atZone(ZoneId.of(eventEntity.timezone))
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = time
            .let(timeFormatter::format)
            .orEmpty()
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
        val eventType = EventType.fromValue(eventEntity.type)
        val textColorResId = if (isVip) R.color.ui_gray else R.color.ui_gray_80
        return EventLabelUiModel(
            textColorResId = textColorResId,
            textSizeSp = TEXT_SIZE_SP,
            imgResId = commonUiMapper.mapEventTypeImgSmallResId(eventType),
            titleResId = mapEventToText(eventType),
            day = dayOfWeek,
            date = mapDateToShortPattern(date),
            time = timeString,
            distanceAddress = null
        )
    }

    fun mapDateToShortPattern(date: LocalDate): String {
        val monthTitle = commonUiMapper.mapMonthShort(date)
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
        return "${date.dayOfMonth} $monthTitle"
    }

    fun mapEventToText(eventType: EventType) = commonUiMapper.mapEventTypeTitleResId(eventType)

    fun mapEventLabelUiModel(
        eventParametersUiModel: EventParametersUiModel,
        isVip: Boolean,
    ): EventLabelUiModel {
        val timeString = eventParametersUiModel.time
            .let(timeFormatter::format)
            .orEmpty()
        val monthTitle = commonUiMapper.mapMonthShort(eventParametersUiModel.date)
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(eventParametersUiModel.date)
        val dateString = "${eventParametersUiModel.date.dayOfMonth} $monthTitle"
        val textColorResId = if (isVip) R.color.ui_gray else R.color.ui_gray_80
        return EventLabelUiModel(
            textColorResId = textColorResId,
            textSizeSp = TEXT_SIZE_SP,
            imgResId = commonUiMapper.mapEventTypeImgSmallResId(eventParametersUiModel.eventType),
            titleResId = commonUiMapper.mapEventTypeTitleResId(eventParametersUiModel.eventType),
            day = dayOfWeek,
            date = dateString,
            time = timeString,
            distanceAddress = null,
            attachment = eventParametersUiModel.model
        )
    }

    fun mapEventPlaceholder(eventUiModel: EventUiModel?): Int? {
        return commonUiMapper.mapEventTypePlaceholderResId(eventUiModel?.eventType)
    }

    fun mapEventLabelUiModel(eventUiModel: EventUiModel, isVip: Boolean): EventLabelUiModel {
        val zoneId = ZoneId.of(eventUiModel.timeZoneId)
        val eventZonedDateTime = Instant.ofEpochMilli(eventUiModel.timestampMs).atZone(zoneId)
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = time
            .let(timeFormatter::format)
            .orEmpty()
        val monthTitle = commonUiMapper.mapMonthShort(date)
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
        val dateString = "${date.dayOfMonth} $monthTitle"
        val textColorResId = if (isVip) R.color.ui_gray else R.color.ui_gray_80
        return EventLabelUiModel(
            textColorResId = textColorResId,
            textSizeSp = TEXT_SIZE_SP,
            imgResId = commonUiMapper.mapEventTypeImgSmallResId(eventUiModel.eventType),
            titleResId = commonUiMapper.mapEventTypeTitleResId(eventUiModel.eventType),
            day = dayOfWeek,
            date = dateString,
            time = timeString,
            distanceAddress = commonUiMapper.mapEventDistanceAddress(eventUiModel.address)
        )
    }

    fun mapEventStatus(event: EventUiModel, isVip: Boolean): EventStatusUiModel? {
        val eventStartInstant = Instant.ofEpochMilli(event.timestampMs)
        val nowInstant = Instant.now()
        val minutesToEventStart = Duration.between(nowInstant, eventStartInstant).toMinutes()
        val eventStartZonedDateTime = Instant.ofEpochMilli(event.timestampMs).atZone(ZoneId.systemDefault())
        val nowZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        val eventStartLocalDate = eventStartZonedDateTime.toLocalDate()
        val nowLocalDate = nowZonedDateTime.toLocalDate()
        val daysToNextWeek = DayOfWeek.SUNDAY.value - nowZonedDateTime.dayOfWeek.value + 1
        val firstDayOfNextWeekZonedDateTime = nowZonedDateTime.plusDays(daysToNextWeek.toLong())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        val firstDayOfWeekAfterNextZonedDateTime = firstDayOfNextWeekZonedDateTime.plusWeeks(1)
        return when {
            minutesToEventStart > 0 && minutesToEventStart < EVENT_STARTING_DURATION_MINUTES -> EventStatusUiModel(
                status = EventStatus.STARTING,
                statusTextResId = R.string.map_events_status_starting,
                textColorResId = if (isVip) R.color.map_event_status_starting_vip else R.color.map_event_status_starting
            )
            minutesToEventStart <= 0 && minutesToEventStart > -EVENT_DURATION_MINUTES -> EventStatusUiModel(
                status = EventStatus.IN_PROGRESS,
                statusTextResId = R.string.map_events_status_in_progress,
                textColorResId = R.color.map_event_status_in_progress
            )
            minutesToEventStart <= -EVENT_DURATION_MINUTES -> EventStatusUiModel(
                status = EventStatus.FINISHED,
                statusTextResId = R.string.map_events_status_finished,
                textColorResId = R.color.map_event_status_finished
            )
            eventStartLocalDate == nowLocalDate -> EventStatusUiModel(
                status = EventStatus.TODAY,
                statusTextResId = R.string.map_events_status_today,
                textColorResId = R.color.map_event_status_today
            )
            eventStartLocalDate.minusDays(1) == nowLocalDate -> EventStatusUiModel(
                status = EventStatus.TOMORROW,
                statusTextResId = R.string.map_events_status_tomorrow,
                textColorResId = R.color.map_event_status_tomorrow
            )
            eventStartZonedDateTime.isBefore(firstDayOfNextWeekZonedDateTime) -> EventStatusUiModel(
                status = EventStatus.THIS_WEEK,
                statusTextResId = R.string.map_events_status_this_week,
                textColorResId = R.color.map_event_status_this_week
            )
            eventStartZonedDateTime.isBefore(firstDayOfWeekAfterNextZonedDateTime) -> EventStatusUiModel(
                status = EventStatus.NEXT_WEEK,
                statusTextResId = R.string.map_events_status_next_week,
                textColorResId = R.color.map_event_status_next_week
            )
            else -> null
        }
    }

    companion object {
        private const val TEXT_SIZE_SP = 16
        private const val EVENT_DURATION_MINUTES = 360
        private const val EVENT_STARTING_DURATION_MINUTES = 60
    }
}
