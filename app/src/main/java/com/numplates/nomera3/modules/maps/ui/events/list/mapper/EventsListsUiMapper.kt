package com.numplates.nomera3.modules.maps.ui.events.list.mapper

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventStatus
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListPageDescription
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListEmptyUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListFiltersUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItemsUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListPageUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListsUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.PagingDataUiModel
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventLabelUiMapper
import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapperImpl
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import javax.inject.Inject

class EventsListsUiMapper @Inject constructor(
    context: Context,
    private val textProcessorUtil: TextProcessorUtil,
    private val eventLabelUiMapper: EventLabelUiMapper,
    private val eventsUiMapper: MapEventsUiMapperImpl
) {
    private val resources = context.resources

    fun mapUiModel(
        defaultEventFiltersMap: Map<EventsListType, EventsListFiltersModel>,
        eventsListsPages: List<EventsListPageDescription>,
        selectedPageIndex: Int,
        eventsListFiltersMap: Map<EventsListType, EventsListFiltersModel>,
        eventsListsItemsMap: Map<EventsListType, EventsListItemsUiModel>,
        canCreateNewEvent: Boolean?
    ): EventsListsUiModel {
        val pages = eventsListsPages.map { page ->
            val eventsListItems = if (canCreateNewEvent == null) {
                getEventsListItemsLoadingStub()
            } else {
                eventsListsItemsMap[page.type] ?: getEventsListItemsDefaultStub()
            }
            val eventListFilters = eventsListFiltersMap[page.type] ?: defaultEventFiltersMap.getValue(page.type)
            val eventFilterType = mapEventFilterTypeUiModel(eventListFilters)
            val eventFilterDate = mapEventFilterDateUiModel(eventListFilters)
            val participationCategoryIndex = mapParticipationCategoryIndex(eventListFilters)
            val nonDefaultFilters = mapNonDefaultFilters(
                eventListFilters,
                defaultEventFiltersMap.getValue(page.type)
            )
            val filters = EventsListFiltersUiModel(
                nonDefaultFilters = nonDefaultFilters,
                eventFilterType = eventFilterType,
                eventFilterDate = eventFilterDate,
                participationCategoryIndex = participationCategoryIndex
            )
            val emptyUiModel = mapPageEmptyUiModel(eventListFilters, canCreateNewEvent ?: true)
            EventsListPageUiModel(
                eventsListType = page.type,
                title = page.title,
                eventsListItems = eventsListItems,
                filters = filters,
                emptyUiModel = emptyUiModel
            )
        }
        return EventsListsUiModel(
            eventsListsPages = pages,
            selectedPageIndex = selectedPageIndex
        )
    }

    fun mapEventsListItems(
        eventsListItems: List<EventsListItem.EventItemUiModel>,
        pagingData: PagingDataUiModel,
        eventsListFilters: EventsListFiltersModel,
        canCreateNewEvent: Boolean?
    ): EventsListItemsUiModel {
        if (canCreateNewEvent == null) return getEventsListItemsLoadingStub()
        val items = when {
            pagingData.isLoadingNextPage -> eventsListItems.plus(getLoadingStubs(eventsListItems.isEmpty()))
            pagingData.isLastPage && eventsListItems.isNotEmpty() -> {
                val emptyItem = mapEmptyItem(
                    eventsListFilters = eventsListFilters,
                    canCreateNewEvent = canCreateNewEvent
                )
                if (emptyItem != null) {
                    eventsListItems.plus(emptyItem)
                } else {
                    eventsListItems
                }
            }
            else -> eventsListItems
        }
        return EventsListItemsUiModel(
            items = items,
            isLoadingNextPage = pagingData.isLoadingNextPage,
            isLastPage = pagingData.isLastPage
        )
    }

    fun mapEventsItemUiModelList(
        items: List<PostEntityResponse>,
        eventsListType: EventsListType
    ): List<EventsListItem.EventItemUiModel> =
        items.mapNotNull {
            mapEventsItemUiModel(it, eventsListType)
        }

    fun mapEventsItemUiModel(
        postEntityResponse: PostEntityResponse,
        eventsListType: EventsListType? = null
    ): EventsListItem.EventItemUiModel? {
        val uiModel = postEntityResponse.toUiEntity(textProcessorUtil = textProcessorUtil)
        val event = uiModel.event ?: return null
        val eventLabel = eventLabelUiMapper.mapEventLabelUiModel(eventUiModel = event, isVip = false)
        val eventStatus = eventLabelUiMapper.mapEventStatus(event = event, isVip = false)
        val participants = EventParticipantsUiModel(
            hostAvatar = uiModel.user?.avatarSmall,
            participantsAvatars = event.participantAvatars,
            participation = event.participation,
            showMap = false,
            isCompact = true,
            isVip = false,
            isFinished = eventStatus?.status == EventStatus.FINISHED
        )
        val eventObject = eventsUiMapper.mapEvent(postEntityResponse) ?: return null
        return EventsListItem.EventItemUiModel(
            eventId = event.id,
            postId = uiModel.postId,
            location = event.address.location,
            eventLabel = eventLabel,
            eventStatus = eventStatus,
            eventTitle = event.title,
            eventTitleTagSpan = event.tagSpan,
            address = event.address.addressString,
            hostAvatar = uiModel.user?.avatarSmall.orEmpty(),
            participants = participants,
            eventObject = eventObject,
            eventsListType = eventsListType
        )
    }

    fun mapEventParticipationCategory(eventParticipationCategoryIndex: Int): EventParticipationCategory =
        getParticipationCategoriesList()[eventParticipationCategoryIndex]

    private fun mapEmptyItem(
        eventsListFilters: EventsListFiltersModel,
        canCreateNewEvent: Boolean
    ): EventsListItem.EmptyItemUiModel? {
        val emptyUiModel = mapListEmptyUiModel(
            eventsListFilters = eventsListFilters,
            canCreateNewEvent = canCreateNewEvent
        ) ?: return null
        val participationCategory = when (eventsListFilters) {
            is EventsListFiltersModel.EventsListFiltersArchiveModel -> eventsListFilters.participationCategory
            is EventsListFiltersModel.EventsListFiltersMyModel -> eventsListFilters.participationCategory
            is EventsListFiltersModel.EventsListFiltersNearbyModel -> null
        }
        return EventsListItem.EmptyItemUiModel(
            eventsListType = eventsListFilters.eventsListType,
            participationCategory = participationCategory,
            uiModel = emptyUiModel
        )
    }

    private fun mapEventFilterTypeUiModel(eventsListFiltersModel: EventsListFiltersModel): EventFilterTypeUiModel {
        val eventTypeFilter = when (eventsListFiltersModel.eventsListType) {
            EventsListType.NEARBY ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersNearbyModel).eventTypeFilter

            EventsListType.MY ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersMyModel).eventTypeFilter

            EventsListType.ARCHIVE ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersArchiveModel).eventTypeFilter
        }
        return EventFilterTypeUiModel(eventTypeFilter)
    }

    private fun mapEventFilterDateUiModel(eventsListFiltersModel: EventsListFiltersModel): EventFilterDateUiModel? {
        val eventDateFilter = when (eventsListFiltersModel.eventsListType) {
            EventsListType.NEARBY ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersNearbyModel).eventDateFilter

            EventsListType.MY ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersMyModel).eventDateFilter

            EventsListType.ARCHIVE -> null
        }
        return eventDateFilter?.let(::EventFilterDateUiModel)
    }

    private fun mapParticipationCategoryIndex(eventsListFiltersModel: EventsListFiltersModel): Int? {
        val participationCategory = when (eventsListFiltersModel.eventsListType) {
            EventsListType.NEARBY -> null
            EventsListType.MY ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersMyModel).participationCategory

            EventsListType.ARCHIVE ->
                (eventsListFiltersModel as EventsListFiltersModel.EventsListFiltersArchiveModel).participationCategory
        }
        return participationCategory?.let { getParticipationCategoriesList().indexOf(it) }
    }

    private fun mapNonDefaultFilters(
        eventsListFilters: EventsListFiltersModel,
        defaultEventListFilters: EventsListFiltersModel
    ): Boolean = when (eventsListFilters.eventsListType) {
        EventsListType.NEARBY -> eventsListFilters != defaultEventListFilters
        EventsListType.MY -> {
            eventsListFilters as EventsListFiltersModel.EventsListFiltersMyModel
            defaultEventListFilters as EventsListFiltersModel.EventsListFiltersMyModel
            eventsListFilters.eventTypeFilter != defaultEventListFilters.eventTypeFilter
                || eventsListFilters.eventDateFilter != defaultEventListFilters.eventDateFilter
        }

        EventsListType.ARCHIVE -> {
            eventsListFilters as EventsListFiltersModel.EventsListFiltersArchiveModel
            defaultEventListFilters as EventsListFiltersModel.EventsListFiltersArchiveModel
            eventsListFilters.eventTypeFilter != defaultEventListFilters.eventTypeFilter
        }
    }

    private fun mapListEmptyUiModel(
        eventsListFilters: EventsListFiltersModel,
        canCreateNewEvent: Boolean
    ): EventsListEmptyUiModel? =
        when (eventsListFilters) {
            is EventsListFiltersModel.EventsListFiltersArchiveModel ->
                mapListArchiveEmptyUiModel(
                    participationCategory = eventsListFilters.participationCategory,
                    canCreateNewEvent = canCreateNewEvent
                )

            is EventsListFiltersModel.EventsListFiltersMyModel ->
                mapListMyEmptyUiModel(
                    participationCategory = eventsListFilters.participationCategory,
                    canCreateNewEvent = canCreateNewEvent
                )

            is EventsListFiltersModel.EventsListFiltersNearbyModel -> mapListNearbyEmptyUiModel(canCreateNewEvent)
        }

    private fun mapListArchiveEmptyUiModel(
        participationCategory: EventParticipationCategory,
        canCreateNewEvent: Boolean
    ): EventsListEmptyUiModel? =
        when (participationCategory) {
            EventParticipationCategory.HOST -> {
                if (canCreateNewEvent) {
                    EventsListEmptyUiModel(
                        title = resources.getString(R.string.map_events_list_empty_title_create_new_event),
                        message = resources.getString(R.string.map_events_list_empty_message_invite_friends),
                        actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                        action = MapUiAction.EventsListUiAction.CreateNewEvent
                    )
                } else {
                    null
                }
            }

            EventParticipationCategory.PARTICIPANT -> EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_see_new_events),
                message = resources.getString(R.string.map_events_list_empty_message_participate),
                actionText = resources.getString(R.string.map_events_list_empty_action_see_events_nearby),
                action = MapUiAction.EventsListUiAction.ShowNearbyPage
            )
        }

    private fun mapListMyEmptyUiModel(
        participationCategory: EventParticipationCategory,
        canCreateNewEvent: Boolean
    ): EventsListEmptyUiModel? =
        when (participationCategory) {
            EventParticipationCategory.HOST -> {
                if (canCreateNewEvent) {
                    EventsListEmptyUiModel(
                        title = resources.getString(R.string.map_events_list_empty_title_create_new_event),
                        message = resources.getString(R.string.map_events_list_empty_message_invite_friends),
                        actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                        action = MapUiAction.EventsListUiAction.CreateNewEvent
                    )
                } else {
                    null
                }
            }

            EventParticipationCategory.PARTICIPANT -> EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_see_new_events),
                message = resources.getString(R.string.map_events_list_empty_message_participate),
                actionText = resources.getString(R.string.map_events_list_empty_action_see_events_nearby),
                action = MapUiAction.EventsListUiAction.ShowNearbyPage
            )
        }

    private fun mapListNearbyEmptyUiModel(canCreateNewEvent: Boolean): EventsListEmptyUiModel? =
        if (canCreateNewEvent) {
            EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_no_suitable_events),
                message = resources.getString(R.string.map_events_list_empty_message_become_host),
                actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                action = MapUiAction.EventsListUiAction.CreateNewEvent
            )
        } else {
            EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_no_events_nearby),
                message = resources.getString(R.string.map_events_list_empty_message_look_elsewhere),
                actionText = null,
                action = null
            )
        }

    private fun mapPageEmptyUiModel(
        eventsListFilters: EventsListFiltersModel,
        canCreateNewEvent: Boolean
    ): EventsListEmptyUiModel =
        when (eventsListFilters) {
            is EventsListFiltersModel.EventsListFiltersArchiveModel ->
                mapPageArchiveEmptyUiModel(
                    participationCategory = eventsListFilters.participationCategory,
                    canCreateNewEvent = canCreateNewEvent
                )

            is EventsListFiltersModel.EventsListFiltersMyModel ->
                mapPageMyEmptyUiModel(eventsListFilters.participationCategory)

            is EventsListFiltersModel.EventsListFiltersNearbyModel -> mapPageNearbyEmptyUiModel(canCreateNewEvent)
        }

    private fun mapPageArchiveEmptyUiModel(
        participationCategory: EventParticipationCategory,
        canCreateNewEvent: Boolean
    ): EventsListEmptyUiModel =
        when (participationCategory) {
            EventParticipationCategory.HOST -> if (canCreateNewEvent) {
                EventsListEmptyUiModel(
                    title = resources.getString(R.string.map_events_list_empty_title_archive_empty),
                    message = resources.getString(R.string.map_events_list_empty_message_create_your_first_event),
                    actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                    action = MapUiAction.EventsListUiAction.CreateNewEvent
                )
            } else {
                EventsListEmptyUiModel(
                    title = resources.getString(R.string.map_events_list_empty_title_this_is_archive),
                    message = resources.getString(R.string.map_events_list_empty_message_this_is_archive),
                    actionText = null,
                    action = null
                )
            }

            EventParticipationCategory.PARTICIPANT -> EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_archive_empty),
                message = resources.getString(R.string.map_events_list_empty_message_see_events_nearby),
                actionText = resources.getString(R.string.map_events_list_empty_action_see_events_nearby),
                action = MapUiAction.EventsListUiAction.ShowNearbyPage
            )
        }

    private fun mapPageMyEmptyUiModel(participationCategory: EventParticipationCategory): EventsListEmptyUiModel =
        when (participationCategory) {
            EventParticipationCategory.HOST -> EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_your_events_list_empty),
                message = resources.getString(R.string.map_events_list_empty_message_create_new_event),
                actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                action = MapUiAction.EventsListUiAction.CreateNewEvent
            )

            EventParticipationCategory.PARTICIPANT -> EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_your_events_list_empty),
                message = resources.getString(R.string.map_events_list_empty_message_see_events_nearby),
                actionText = resources.getString(R.string.map_events_list_empty_action_see_events_nearby),
                action = MapUiAction.EventsListUiAction.ShowNearbyPage
            )
        }

    private fun mapPageNearbyEmptyUiModel(canCreateNewEvent: Boolean): EventsListEmptyUiModel =
        if (canCreateNewEvent) {
            EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_second_empty_title_no_events_nearby),
                message = resources.getString(R.string.map_events_list_empty_message_become_host_first),
                actionText = resources.getString(R.string.map_events_list_empty_action_create_new),
                action = MapUiAction.EventsListUiAction.CreateNewEvent
            )
        } else {
            EventsListEmptyUiModel(
                title = resources.getString(R.string.map_events_list_empty_title_no_events_nearby),
                message = resources.getString(R.string.map_events_list_empty_message_look_elsewhere),
                actionText = null,
                action = null
            )
        }

    private fun getEventsListItemsLoadingStub(): EventsListItemsUiModel =
        EventsListItemsUiModel(
            items = getLoadingStubs(true),
            isLoadingNextPage = true,
            isLastPage = false
        )

    private fun getEventsListItemsDefaultStub(): EventsListItemsUiModel =
        EventsListItemsUiModel(
            items = emptyList(),
            isLoadingNextPage = false,
            isLastPage = false
        )

    private fun getLoadingStubs(isInitial: Boolean): List<EventsListItem.StubItemUiModel> =
        listOf(
            EventsListItem.StubItemUiModel(isInitial = isInitial, position = 0),
            EventsListItem.StubItemUiModel(isInitial = isInitial, position = 1),
            EventsListItem.StubItemUiModel(isInitial = isInitial, position = 2),
        )

    private fun getParticipationCategoriesList(): List<EventParticipationCategory> =
        listOf(
            EventParticipationCategory.PARTICIPANT,
            EventParticipationCategory.HOST
        )
}
