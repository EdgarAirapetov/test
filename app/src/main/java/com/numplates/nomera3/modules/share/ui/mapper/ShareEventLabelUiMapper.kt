package com.numplates.nomera3.modules.share.ui.mapper

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ShareEventLabelUiMapper @Inject constructor(private val commonUiMapper: EventsCommonUiMapper) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun mapEventLabelUiModel(event: EventUiModel): EventLabelUiModel {
        val zoneId = ZoneId.of(event.timeZoneId)
        val eventZonedDateTime = Instant.ofEpochMilli(event.timestampMs).atZone(zoneId)
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = time
            .let(timeFormatter::format)
            .orEmpty()
        val monthTitle = commonUiMapper.mapMonthShort(date)
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
        val dateString = "${date.dayOfMonth} $monthTitle"
        return EventLabelUiModel(
            textColorResId = R.color.shareEventLabel,
            textSizeSp = TEXT_SIZE_SP,
            imgResId = commonUiMapper.mapEventTypeImgSmallResId(event.eventType),
            titleResId = commonUiMapper.mapEventTypeTitleResId(event.eventType),
            day = dayOfWeek,
            date = dateString,
            time = timeString,
            distanceAddress = null
        )
    }

    companion object {
        private const val TEXT_SIZE_SP = 14
    }
}
