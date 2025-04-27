package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMap
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.AddressSearchState
import com.numplates.nomera3.modules.maps.ui.events.model.AddressUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationMarkerState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetDataUiState
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetItem
import com.numplates.nomera3.modules.maps.ui.events.model.EventTimeUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemColorSchemeUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.mapper.LocationUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapCameraState
import com.numplates.nomera3.modules.maps.ui.snippet.model.DataFetchingStateModel
import com.numplates.nomera3.modules.newroads.data.entities.EventEntity
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventConfigurationUiMode
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import com.numplates.nomera3.modules.uploadpost.ui.mapper.UIAttachmentsMapper
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject

class MeeraMapEventsUiMapperImpl @Inject constructor(
    context: Context,
    private val locationMapper: LocationUiMapperImpl,
    private val textProcessorUtil: TextProcessorUtil,
    private val commonUiMapper: EventsCommonUiMapper,
    private val uiAttachmentsMapper: UIAttachmentsMapper
) : MeeraMapEventsUiMapper {

    private val resources = context.resources
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun mapEventConfigurationState(
        eventConfigurationUiMode: MeeraEventConfigurationUiMode,
        markerState: EventConfigurationMarkerState,
        selectedType: EventType,
        eventTime: EventTimeUiModel,
        isMyLocationActive: Boolean,
    ): EventConfigurationState {
        Timber.e("EventConfigurationState $eventConfigurationUiMode")
        return when (eventConfigurationUiMode) {
            MeeraEventConfigurationUiMode.ONBOARDING -> EventConfigurationState.Onboarding(
                eventTypeItems = mapEventTypes(selectedType)
            )

            MeeraEventConfigurationUiMode.OPEN, MeeraEventConfigurationUiMode.HIDDEN -> {
                val isInUserTimeZone = eventTime.timeZone.rawOffset == TimeZone.getDefault().rawOffset
                val timeString = eventTime.time
                    .let(timeFormatter::format)
                    .let {
                        if (isInUserTimeZone) it else "$it (${resources.getString(R.string.map_events_timezone_hint)})"
                    }
                val eventDateItems = mapDateItems(minimumDate = eventTime.minimumDate, selectedDate = eventTime.date)
//                EventConfigurationState.Configuration(
//                    eventTypeItems = mapEventTypes(selectedType),
//                    eventDateItems = eventDateItems,
//                    selectedTime = timeString,
//                    markerState = markerState,
//                    isContinueEnabled = markerState is EventConfigurationMarkerState.Address,
//                    isHidden = eventConfigurationUiMode == EventConfigurationUiMode.HIDDEN,
//                    isMyLocationActive = isMyLocationActive
//                )
                EventConfigurationState.StepFirstConfiguration(
                    eventTypeItems = mapEventTypes(selectedType),
                    eventDateItems = eventDateItems,
                    selectedTime = timeString,
                    markerState = markerState,
                    isContinueEnabled = markerState is EventConfigurationMarkerState.Address,
                    isHidden = eventConfigurationUiMode == MeeraEventConfigurationUiMode.HIDDEN,
                    isMyLocationActive = isMyLocationActive,
                    time = eventTime.time
                )
            }

            MeeraEventConfigurationUiMode.FIRST_STEP -> {
                val isInUserTimeZone = eventTime.timeZone.rawOffset == TimeZone.getDefault().rawOffset
                val timeString = eventTime.time
                    .let(timeFormatter::format)
                    .let {
                        if (isInUserTimeZone) it else "$it (${resources.getString(R.string.map_events_timezone_hint)})"
                    }
                val eventDateItems = mapDateItems(minimumDate = eventTime.minimumDate, selectedDate = eventTime.date)
                EventConfigurationState.StepFirstConfiguration(
                    eventTypeItems = mapEventTypes(selectedType),
                    eventDateItems = eventDateItems,
                    selectedTime = timeString,
                    markerState = markerState,
                    isContinueEnabled = markerState is EventConfigurationMarkerState.Address,
                    isHidden = eventConfigurationUiMode == MeeraEventConfigurationUiMode.HIDDEN,
                    isMyLocationActive = isMyLocationActive,
                    time = eventTime.time
                )
            }

            MeeraEventConfigurationUiMode.CLOSED -> EventConfigurationState.Closed
            MeeraEventConfigurationUiMode.STEP1_FINISHED -> {
                EventConfigurationState.Step2(
                    markerState = markerState
                )
            }

            is MeeraEventConfigurationUiMode.STEP2_FINISHED -> {
                val isInUserTimeZone = eventTime.timeZone.rawOffset == TimeZone.getDefault().rawOffset
                val timeString = eventTime.time
                    .let(timeFormatter::format)
                    .let {
                        if (isInUserTimeZone) it else "$it (${resources.getString(R.string.map_events_timezone_hint)})"
                    }
                val eventDateItems = mapDateItems(minimumDate = eventTime.minimumDate, selectedDate = eventTime.date)
                EventConfigurationState.Configuration(
                    eventTypeItems = mapEventTypes(selectedType),
                    eventDateItems = eventDateItems,
                    selectedTime = timeString,
                    markerState = markerState,
                    isContinueEnabled = true,
                    imageAttachment = uiAttachmentsMapper.mapImageToAttachment(
                        eventConfigurationUiMode.imageUri?.path.orEmpty(),
                        true
                    ),
//                    isContinueEnabled = markerState is EventConfigurationMarkerState.Address,
                    isHidden = eventConfigurationUiMode == MeeraEventConfigurationUiMode.HIDDEN,
                    isMyLocationActive = isMyLocationActive
                )
            }

            MeeraEventConfigurationUiMode.EMPTY -> EventConfigurationState.Empty
        }
    }

    override fun mapDateItems(minimumDate: LocalDate, selectedDate: LocalDate): List<EventDateItemUiModel> {
        return (0 until DATE_ITEMS_COUNT).map { index ->
            val date = minimumDate.plusDays(index.toLong())
            val monthTitle = commonUiMapper.mapMonthShort(date)
            val dayOfWeek = commonUiMapper.mapDayOfWeekShort(date)
            EventDateItemUiModel(
                date = date,
                dateString = "${date.dayOfMonth} $monthTitle",
                dayOfWeek = dayOfWeek,
                selected = date == selectedDate
            )
        }
    }

    override fun mapMarkerState(
        eventPlace: PlaceModel?,
        addressSearchState: AddressSearchState,
        mapCameraState: MapCameraState
    ): EventConfigurationMarkerState {
        return when (mapCameraState) {
            MapCameraState.Idle -> {
                when (addressSearchState) {
                    is AddressSearchState.Success -> if (eventPlace != null) {
                        val markerAddress = eventPlace.name
                            .ifEmpty { resources.getString(R.string.map_events_no_address) }
                        EventConfigurationMarkerState.Address(markerAddress)
                    } else {
                        EventConfigurationMarkerState.Error
                    }

                    AddressSearchState.Error -> EventConfigurationMarkerState.Error
                    else -> EventConfigurationMarkerState.Progress(false)
                }
            }

            is MapCameraState.Moving -> EventConfigurationMarkerState.Progress(true)
        }
    }

    override fun mapEventParametersUiModel(
        eventPlace: PlaceModel,
        eventTime: EventTimeUiModel,
        eventType: EventType,
        model: UIAttachmentPostModel?
    ): EventParametersUiModel {
        return EventParametersUiModel(
            address = mapAddress(eventPlace),
            date = eventTime.date,
            time = eventTime.time,
            timeZoneId = eventTime.timeZone.id,
            eventType = eventType,
            placeId = eventPlace.placeId,
            model = model
        )
    }

    override fun mapEventEntity(eventUiModel: EventParametersUiModel): EventEntity {
        val date = LocalDateTime.of(eventUiModel.date, eventUiModel.time)
            .atZone(ZoneId.of(eventUiModel.timeZoneId))
            .toInstant()
            .toString()
        val timeZone = eventUiModel.timeZoneId.removePrefix("GMT")
        return EventEntity(
            title = "",
            name = eventUiModel.address.name,
            address = eventUiModel.address.addressString,
            latitude = eventUiModel.address.location.latitude,
            longitude = eventUiModel.address.location.longitude,
            date = date,
            timezone = timeZone,
            type = eventUiModel.eventType.value,
            placeId = eventUiModel.placeId
        )
    }

    override fun mapEvents(posts: List<PostEntityResponse>): List<EventObjectUiModel> {
        return posts.mapNotNull(::mapEvent)
    }

    override fun mapEvent(postEntityResponse: PostEntityResponse): EventObjectUiModel? {
        val post = postEntityResponse.toUiEntity(
            textProcessorUtil = textProcessorUtil,
            isInSnippet = true,
            isNotExpandedSnippetState = true
        )
        val eventUiModel = post.event ?: return null
        val eventIconResId = commonUiMapper.mapEventTypeImgResId(eventUiModel.eventType)
        val eventPinIconResId = commonUiMapper.mapEventTypeImgPinResId(eventUiModel.eventType)
        val eventColorResId = commonUiMapper.mapEventTypeColorResId(eventUiModel.eventType)
        return EventObjectUiModel(
            eventIconResId = eventIconResId,
            eventPinIconResId = eventPinIconResId,
            eventColorResId = eventColorResId,
            eventPost = post
        )
    }

    override fun mapEventEditingSetupUiModel(eventEntity: EventEntity): EventEditingSetupUiModel {
        val zoneId = ZoneId.of(eventEntity.timezone)
        val eventZonedDateTime = Instant.parse(eventEntity.date).atZone(zoneId)
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val eventType = EventType.fromValue(eventEntity.type)
        val place = PlaceModel(
            addressString = eventEntity.address,
            name = eventEntity.name,
            timeZone = TimeZone.getTimeZone(zoneId),
            location = CoordinatesModel(
                lat = eventEntity.latitude,
                lon = eventEntity.longitude
            ),
            placeId = eventEntity.placeId,
//            imageUri =
        )
        return EventEditingSetupUiModel(
            place = place,
            date = date,
            time = time,
            eventType = eventType
        )
    }

    override fun mapEventTime(
        minimumTimeInstant: Instant,
        time: LocalTime?,
        date: LocalDate?,
        place: PlaceModel?
    ): EventTimeUiModel {
        val timeZone = place?.timeZone ?: TimeZone.getDefault()
        val minimumZonedDateTime = minimumTimeInstant
            .atZone(timeZone.toZoneId())
        val minimumDate = minimumZonedDateTime.toLocalDate()
        val minimumTime = minimumZonedDateTime.toLocalTime()
//        val isCurrentDate = minimumDate.isAfter(date) || minimumDate.isEqual(date)
        val eventTime = if (date == null || time == null
            || (minimumTime.isAfter(time) &&
                (minimumDate.isAfter(date) || minimumDate.isEqual(date)))
        ) {
            minimumTime
        } else {
            time
        }
        val eventDate = if (date == null || time == null) {
            minimumDate
        } else {
            date
        }
        return EventTimeUiModel(
            minimumTime = minimumTime,
            minimumDate = minimumDate,
            time = eventTime,
            date = eventDate,
            timeZone = timeZone
        )
    }

    override fun mapEventSnippetUiModel(
        selectedEvent: EventObjectUiModel?,
        eventPages: Map<Int, List<EventObjectUiModel>>,
        dataFetchingState: DataFetchingStateModel
    ): EventSnippetDataUiState {
        val event = selectedEvent?.eventPost?.event ?: return EventSnippetDataUiState.Empty
        val isPreloadedItem = eventPages.isEmpty()
        return when {
            isPreloadedItem && dataFetchingState.error == null -> {
                val item = EventSnippetItem.EventPostItem(
                    eventObject = selectedEvent,
                    updateWhenCreated = false,
                    snippetHeight = 0
                )
                EventSnippetDataUiState.PreloadedSnippet(item = item, isAuxSnippet = true)
            }

            isPreloadedItem && dataFetchingState.error != null -> {
                val item = EventSnippetItem.ErrorItem(event.address.location)
                EventSnippetDataUiState.Error(item)
            }

            else -> {
                val sortedPages = eventPages.entries
                    .sortedBy { it.key }
                    .map { it.value }
                val events = sortedPages.flatten().map { eventObject ->
                    EventSnippetItem.EventPostItem(
                        eventObject = eventObject,
                        updateWhenCreated = false,
                        snippetHeight = 0
                    )
                }
                val items =
                    if (sortedPages.lastOrNull()?.size == EventsOnMap.EVENT_SNIPPET_PAGE_SIZE && dataFetchingState.loading) {
                        events.plus(EventSnippetItem.LoaderItem)
                    } else {
                        events
                    }
                EventSnippetDataUiState.SnippetList(items = items)
            }
        }
    }

    override fun mapAuxEventSnippetUiModel(eventObject: EventObjectUiModel?): EventSnippetDataUiState {
        if (eventObject == null) return EventSnippetDataUiState.Empty
        val item = EventSnippetItem.EventPostItem(
            eventObject = eventObject,
            updateWhenCreated = true,
            snippetHeight = 0
        )
        return EventSnippetDataUiState.PreloadedSnippet(item = item, isAuxSnippet = true)
    }

    override fun mapEventObjectUiModel(post: PostUIEntity): EventObjectUiModel? {
        val eventUiModel = post.event ?: return null
        val eventPost = post.copy(
            isNotExpandedSnippetState = true,
            tagSpan = textProcessorUtil.calculateTextLineCount(
                tagSpan = post.tagSpan,
                isMedia = post.containsMedia(),
                isInSnippet = true
            ),
        )
        val eventIconResId = commonUiMapper.mapEventTypeImgResId(eventUiModel.eventType)
        val eventPinIconResId = commonUiMapper.mapEventTypeImgPinResId(eventUiModel.eventType)
        val eventColorResId = commonUiMapper.mapEventTypeColorResId(eventUiModel.eventType)
        return EventObjectUiModel(
            eventIconResId = eventIconResId,
            eventPinIconResId = eventPinIconResId,
            eventColorResId = eventColorResId,
            eventPost = eventPost
        )
    }

    override fun mapTimePickerModel(eventTime: EventTimeUiModel): TimePickerUiModel {
        val isInUserTimeZone = eventTime.timeZone.rawOffset == TimeZone.getDefault().rawOffset
        return TimePickerUiModel(
            minimumTime = if (eventTime.date == eventTime.minimumDate) eventTime.minimumTime else null,
            selectedTime = eventTime.time,
            isInUserTimezone = isInUserTimeZone
        )
    }

    override fun mapEventStartInstant(eventEditingSetupUiModel: EventEditingSetupUiModel): Instant =
        LocalDateTime.of(eventEditingSetupUiModel.date, eventEditingSetupUiModel.time)
            .atZone(eventEditingSetupUiModel.place.timeZone.toZoneId())
            .toInstant()

    private fun mapAddress(place: PlaceModel): AddressUiModel {
        return AddressUiModel(
            name = place.name.ifEmpty { resources.getString(R.string.map_events_no_address) },
            addressString = place.addressString,
            location = locationMapper.mapLatLng(place.location),
            timeZoneId = place.timeZone.id,
            distanceMeters = null
        )
    }

    private fun mapEventTypes(selectedType: EventType): List<EventTypeItemUiModel> {
        return EventType.values().map { type ->
            EventTypeItemUiModel(
                type = type,
                selected = type == selectedType,
                titleResId = commonUiMapper.mapEventTypeTitleResId(type),
                imgResId = commonUiMapper.mapEventTypeImgResId(type),
                selectedColorScheme = mapEventTypeItemColorScheme(type)
            )
        }
    }

    private fun mapEventTypeItemColorScheme(eventType: EventType): EventTypeItemColorSchemeUiModel {
        return when (eventType) {
            EventType.EDUCATION -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_education_item_bg,
                outlineColorResId = R.color.map_event_education_item_outline,
                textColorResId = R.color.map_event_education_item_text
            )

            EventType.ART -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_art_bg,
                outlineColorResId = R.color.map_event_art_item_outline,
                textColorResId = R.color.map_event_art_item_text
            )

            EventType.CONCERT -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_concert_item_bg,
                outlineColorResId = R.color.map_event_concert_item_outline,
                textColorResId = R.color.map_event_concert_item_text
            )

            EventType.SPORT -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_sport_item_bg,
                outlineColorResId = R.color.map_event_sport_item_outline,
                textColorResId = R.color.map_event_sport_item_text
            )

            EventType.TOURISM -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_tourism_item_bg,
                outlineColorResId = R.color.map_event_tourism_item_outline,
                textColorResId = R.color.map_event_tourism_item_text
            )

            EventType.GAMES -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_games_item_bg,
                outlineColorResId = R.color.map_event_games_item_outline,
                textColorResId = R.color.map_event_games_item_text
            )

            EventType.PARTY -> EventTypeItemColorSchemeUiModel(
                backgroundColorResId = R.color.map_event_party_item_bg,
                outlineColorResId = R.color.map_event_party_item_outline,
                textColorResId = R.color.map_event_party_item_text
            )
        }
    }

    companion object {
        private const val DATE_ITEMS_COUNT = 9
    }
}
