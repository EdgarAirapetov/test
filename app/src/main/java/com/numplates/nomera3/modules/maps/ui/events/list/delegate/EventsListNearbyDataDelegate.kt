package com.numplates.nomera3.modules.maps.ui.events.list.delegate

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsListWhere
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListNearbyParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetDefaultEventsListFiltersNearbyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListNearbyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.SetEventsListFiltersUseCase
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.mapper.EventsListsUiMapper
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EventsListNearbyDataDelegate(
    private val scope: CoroutineScope,
    private val canCreateNewEventFlow: Flow<Boolean?>,
    private val getEventsListNearbyUseCase: GetEventsListNearbyUseCase,
    private val getDefaultEventsListFiltersNearbyUseCase: GetDefaultEventsListFiltersNearbyUseCase,
    private val setEventsListFiltersUseCase: SetEventsListFiltersUseCase,
    private val uiMapper: EventsListsUiMapper,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor
) {
    private var filters: EventsListFiltersModel.EventsListFiltersNearbyModel =
        getDefaultEventsListFiltersNearbyUseCase.invoke()

    private val pagingDelegate = EventsItemsListPagingDelegate(scope) { offset, limit ->
        val params = GetEventsListNearbyParamsModel(
            eventTypes = filters.eventTypeFilter,
            timeFilter = filters.eventDateFilter,
            offset = offset,
            limit = limit
        )
        uiMapper.mapEventsItemUiModelList(getEventsListNearbyUseCase.invoke(params), EventsListType.NEARBY)
    }
    private var eventsListEmptyItemReached = false

    val itemsFlow = combine(
        pagingDelegate.getItemsFlow(),
        pagingDelegate.getPagingDataFlow(),
        canCreateNewEventFlow
    ) { items, pagingData, canCreateNewEvent  ->
        uiMapper.mapEventsListItems(
            eventsListItems = items,
            pagingData = pagingData,
            eventsListFilters = filters,
            canCreateNewEvent = canCreateNewEvent
        )
    }

    fun setEventFilters(eventFiltersUpdate: EventFiltersUpdateUiModel) {
        val updatedFilters = filters.copy(
            eventTypeFilter = eventFiltersUpdate.eventFilterType.selectedEventTypes,
            eventDateFilter = eventFiltersUpdate.eventFilterDate.selectedFilterEventDate
        )
        updateFilters(updatedFilters)
    }

    fun reset() {
        clearEventFilters()
        clearItems()
    }

    fun fetchNextPage() = pagingDelegate.fetchNextPage()

    fun removeItem(postId: Long) {
        pagingDelegate.removeItem(postId)
    }

    fun handleEventsListEmptyItemReachedAnalytics() {
        if (eventsListEmptyItemReached.not()) {
            eventsListEmptyItemReached = true
            mapEventsAnalyticsInteractor.logMapEventsListPopupShown(AmplitudePropertyMapEventsListWhere.NEARBY)
        }
    }

    private fun clearItems() {
        eventsListEmptyItemReached = false
        pagingDelegate.clear()
    }

    private fun clearEventFilters() {
        val defaultFilters = getDefaultEventsListFiltersNearbyUseCase.invoke()
        filters = defaultFilters
        setEventsListFiltersUseCase.invoke(filters)
    }

    private fun updateFilters(updatedFilters: EventsListFiltersModel.EventsListFiltersNearbyModel) {
        val needToReset = filters != updatedFilters
        filters = updatedFilters
        setEventsListFiltersUseCase.invoke(updatedFilters)
        if (needToReset) {
            clearItems()
            pagingDelegate.fetchNextPage(true)
        }
    }
}
