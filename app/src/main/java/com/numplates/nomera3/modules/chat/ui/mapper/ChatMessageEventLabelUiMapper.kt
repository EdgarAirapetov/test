package com.numplates.nomera3.modules.chat.ui.mapper

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Deprecated("This class used in legacy code. Create new one for redesigned app.")
class ChatMessageEventLabelUiMapper @Inject constructor(private val commonUiMapper: EventsCommonUiMapper) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun mapEventLabelUiModel(eventDto: EventDto, isSenderMessage: Boolean): EventLabelUiModel {
        val eventZonedDateTime = Instant.parse(eventDto.startTime).atZone(ZoneId.of(eventDto.address.timeZone))
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = time
            .let(timeFormatter::format)
            .orEmpty()
        val monthTitle = commonUiMapper.mapMonthShort(date)
        val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
        val dateString = "${date.dayOfMonth} $monthTitle"
        val eventType = EventType.fromValue(eventDto.eventType)
        val textColorResId = if (isSenderMessage) R.color.senderEventLabel else R.color.receiverEventLabel
        return EventLabelUiModel(
            textColorResId = textColorResId,
            textSizeSp = TEXT_SIZE_SP,
            imgResId = commonUiMapper.mapEventTypeImgSmallResId(eventType),
            titleResId = commonUiMapper.mapEventTypeTitleResId(eventType),
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
