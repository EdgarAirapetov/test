package com.numplates.nomera3.modules.maps.ui.events.list.delegate

import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsListWhere
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.GetEventsListArchiveParamsModel
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetDefaultEventsListFiltersArchiveUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListArchiveUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.SetEventsListFiltersUseCase
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.ui.events.list.mapper.EventsListsUiMapper
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class EventsListArchiveDataDelegate(
    private val scope: CoroutineScope,
    private val canCreateNewEventFlow: Flow<Boolean?>,
    private val getEventsListArchiveUseCase: GetEventsListArchiveUseCase,
    private val getDefaultEventsListFiltersArchiveUseCase: GetDefaultEventsListFiltersArchiveUseCase,
    private val setEventsListFiltersUseCase: SetEventsListFiltersUseCase,
    private val uiMapper: EventsListsUiMapper,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor
) {
    private var filters: EventsListFiltersModel.EventsListFiltersArchiveModel =
        getDefaultEventsListFiltersArchiveUseCase.invoke()
    private val pagingDelegate = EventsItemsListPagingDelegate(scope) { offset, limit ->
        val params = GetEventsListArchiveParamsModel(
            eventTypes = filters.eventTypeFilter,
            category = filters.participationCategory,
            offset = offset,
            limit = limit
        )
        uiMapper.mapEventsItemUiModelList(getEventsListArchiveUseCase.invoke(params), EventsListType.ARCHIVE)
    }
    private var eventsListEmptyItemReached = false

    val itemsFlow = combine(
        pagingDelegate.getItemsFlow(),
        pagingDelegate.getPagingDataFlow(),
        canCreateNewEventFlow
    ) { items, pagingData, canCreateNewEvent ->
        uiMapper.mapEventsListItems(
            eventsListItems = items,
            pagingData = pagingData,
            eventsListFilters = filters,
            canCreateNewEvent
        )
    }

    fun getFilters(): EventsListFiltersModel.EventsListFiltersArchiveModel = filters

    fun setEventFilters(eventFiltersUpdate: EventFiltersUpdateUiModel) {
        val updatedFilters = filters.copy(eventTypeFilter = eventFiltersUpdate.eventFilterType.selectedEventTypes)
        updateFilters(updatedFilters)
    }

    fun setParticipationCategory(participationCategory: EventParticipationCategory) {
        val updatedFilters = filters.copy(participationCategory = participationCategory)
        updateFilters(updatedFilters)
    }

    fun reset() {
        clearAllFilters()
        clearItems()
    }

    fun fetchNextPage() = pagingDelegate.fetchNextPage()

    fun removeItem(postId: Long) = pagingDelegate.removeItem(postId)

    fun handleEventsListEmptyItemReachedAnalytics() {
        if (eventsListEmptyItemReached.not()) {
            eventsListEmptyItemReached = true
            val where = when(filters.participationCategory) {
                EventParticipationCategory.HOST -> AmplitudePropertyMapEventsListWhere.ARCHIVE_CREATOR
                EventParticipationCategory.PARTICIPANT -> AmplitudePropertyMapEventsListWhere.ARCHIVE_MEMBER
            }
            mapEventsAnalyticsInteractor.logMapEventsListPopupShown(where)
        }
    }

    private fun clearItems() {
        eventsListEmptyItemReached = false
        pagingDelegate.clear()
    }

    private fun clearAllFilters() {
        val defaultFilters = getDefaultEventsListFiltersArchiveUseCase.invoke()
        filters = defaultFilters
        setEventsListFiltersUseCase.invoke(defaultFilters)
    }

    private fun updateFilters(updatedFilters: EventsListFiltersModel.EventsListFiltersArchiveModel) {
        val needToReset = filters != updatedFilters
        filters = updatedFilters
        setEventsListFiltersUseCase.invoke(updatedFilters)
        if (needToReset) {
            clearItems()
            pagingDelegate.fetchNextPage(true)
        }
    }
}
