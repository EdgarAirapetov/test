package com.numplates.nomera3.modules.maps.ui.events.list.delegate

import com.meera.core.extensions.isNotTrue
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGetThereWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsWantToGoWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetDefaultEventsListFiltersArchiveUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetDefaultEventsListFiltersMyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetDefaultEventsListFiltersNearbyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListArchiveUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListMyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListNearbyUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.GetEventsListsPagesListUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.ObserveEventsListFiltersMapUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.SetEventListCoordinatesUseCase
import com.numplates.nomera3.modules.maps.domain.events.list.usecase.SetEventsListFiltersUseCase
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListPageDescription
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetAvailableMapEventCountFromLocalProfileUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetAvailableMapEventCountUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.JoinEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.LeaveEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.ObserveEventParticipationChangesUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.ui.events.list.mapper.EventsListsUiMapper
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventFiltersUpdateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItem
import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListItemsUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.MapEventsListsDelegateConfigUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.model.SelectedEventsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapUiFactor
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MapEventsListsDelegate @Inject constructor(
    private val getMapSettingsUseCase: GetMapSettingsUseCase,
    private val setEventsListFiltersUseCase: SetEventsListFiltersUseCase,
    private val observeEventsListFiltersMapUseCase: ObserveEventsListFiltersMapUseCase,
    private val getDefaultEventsListFiltersNearbyUseCase: GetDefaultEventsListFiltersNearbyUseCase,
    private val getDefaultEventsListFiltersMyUseCase: GetDefaultEventsListFiltersMyUseCase,
    private val getDefaultEventsListFiltersArchiveUseCase: GetDefaultEventsListFiltersArchiveUseCase,
    private val getEventsListsPagesListUseCase: GetEventsListsPagesListUseCase,
    private val getEventsListNearbyUseCase: GetEventsListNearbyUseCase,
    private val getEventsListMyUseCase: GetEventsListMyUseCase,
    private val getEventsListArchiveUseCase: GetEventsListArchiveUseCase,
    private val joinEventUseCase: JoinEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase,
    private val uiMapper: EventsListsUiMapper,
    private val observeEventParticipationChangesUseCase: ObserveEventParticipationChangesUseCase,
    private val getAvailableMapEventCountUseCase: GetAvailableMapEventCountUseCase,
    private val getAvailableMapEventCountFromLocalProfileUseCase: GetAvailableMapEventCountFromLocalProfileUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor,
    private val mapAnalyticsInteractor: MapAnalyticsInteractor,
    private val setEventListCoordinatesUseCase: SetEventListCoordinatesUseCase
) {
    private var initJob: Job? = null
    private var lastFocusedEventPostId: Long? = null
    private var listTypeToAnimateFirstItemFocus: EventsListType? = EventsListType.NEARBY
    private var eventsListNearbyDataDelegate: EventsListNearbyDataDelegate? = null
    private var eventsListMyDataDelegate: EventsListMyDataDelegate? = null
    private var eventsListArchiveDataDelegate: EventsListArchiveDataDelegate? = null
    private var config: MapEventsListsDelegateConfigUiModel? = null
    private val selectedItemPositionsMap = mutableMapOf<EventsListType, Int>()
    private val eventsListsPages = getEventsListsPagesListUseCase.invoke()
    private val selectedPageIndexFlow = MutableStateFlow(DEFAULT_SELECTED_PAGE_INDEX)
    private val eventsListsItemsFlow = MutableStateFlow(mapOf<EventsListType, EventsListItemsUiModel>())
    private val defaultFiltersMap = mapOf(
        EventsListType.NEARBY to getDefaultEventsListFiltersNearbyUseCase.invoke(),
        EventsListType.MY to getDefaultEventsListFiltersMyUseCase.invoke(),
        EventsListType.ARCHIVE to getDefaultEventsListFiltersArchiveUseCase.invoke()
    )
    private val canCreateNewEventFlow = MutableStateFlow<Boolean?>(null)
    val showShareDialogFlow = MutableStateFlow<PostUIEntity?>(null)
    val uiModel = combine(
        getInitialValuesCompositeFlow(),
        selectedPageIndexFlow,
        observeEventsListFiltersMapUseCase.invoke(),
        eventsListsItemsFlow,
        canCreateNewEventFlow
    ) { initialValuesComposite, selectedPageIndex, eventsListFiltersMap, eventsListsItemsMap, canCreateNewEvent ->
        val (defaultEventFilters, eventsListsPages) = initialValuesComposite
        uiMapper.mapUiModel(
            defaultEventFiltersMap = defaultEventFilters,
            eventsListsPages = eventsListsPages,
            selectedPageIndex = selectedPageIndex,
            eventsListFiltersMap = eventsListFiltersMap,
            eventsListsItemsMap = eventsListsItemsMap,
            canCreateNewEvent = canCreateNewEvent
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()

    fun initialize(config: MapEventsListsDelegateConfigUiModel) {
        this.config = config
        initializeEventsListNearbyDataDelegate(config)
        initializeEventsListMyDataDelegate(config)
        initializeEventsListArchiveDataDelegate(config)
        observeEventParticipationChanges(config)
    }

    fun handleEventsListsUiAction(uiAction: MapUiAction.EventsListUiAction) {
        when (uiAction) {
            is MapUiAction.EventsListUiAction.EventsListItemSelected -> handleEventsListItemSelected(uiAction.item)
            MapUiAction.EventsListUiAction.EventsListsClosed -> handleEventsListsClosed()
            MapUiAction.EventsListUiAction.EventsListPressed -> handleEventsListPressed()
            is MapUiAction.EventsListUiAction.EventFiltersChanged -> handleEventFiltersChanged(uiAction.eventFiltersUpdate)
            is MapUiAction.EventsListUiAction.EventParticipationCategoryChanged -> {
                handleEventParticipationChanged(uiAction.participationCategoryIndex)
            }
            is MapUiAction.EventsListUiAction.SelectedPageChanged -> handleSelectedPageChanged(uiAction.index)
            is MapUiAction.EventsListUiAction.LoadNextListPageRequested -> handleLoadNextPage(uiAction.type)
            MapUiAction.EventsListUiAction.CreateNewEvent -> handleCreateNewEvent()
            is MapUiAction.EventsListUiAction.JoinEvent -> {
                handleJoinEvent(uiAction.eventItem)
            }

            is MapUiAction.EventsListUiAction.LeaveEvent -> {
                handleLeaveEvent(uiAction.eventItem)
            }

            is MapUiAction.EventsListUiAction.NavigateToEvent -> handleNavigateToEvent(uiAction.eventItem)
            is MapUiAction.EventsListUiAction.OpenEventPost -> handleOpenEventPost(uiAction.eventItem)
            is MapUiAction.EventsListUiAction.ShowEventHostProfile -> handleShowEventHostProfile(uiAction.eventItem)
            is MapUiAction.EventsListUiAction.ShowEventParticipants -> handleShowEventParticipants(uiAction.eventItem)
            is MapUiAction.EventsListUiAction.EventsListItemDeleted -> handleEventsListItemDeleted(uiAction.postId)
            MapUiAction.EventsListUiAction.EventsListItemDetailsCloseClicked -> handleEventsListItemDetailsCloseClicked()
            is MapUiAction.EventsListUiAction.ShowEventCreator -> handleShowEventCreator(uiAction.eventItem)
            MapUiAction.EventsListUiAction.ShowNearbyPage -> {
                handleShowNearbyPage()
            }

            MapUiAction.EventsListUiAction.ShowNearbyPageWithRefresh -> {
                handleShowNearbyPage()
                eventsListNearbyDataDelegate?.reset()
            }

            MapUiAction.EventsListUiAction.EventPostClosed -> handleEventPostClosed()
            is MapUiAction.EventsListUiAction.CameraChanged -> {
                setEventListCoordinatesUseCase.invoke(uiAction.latLng)
            }
        }
    }

    private fun handleEventPostClosed() {
        val config = config ?: return
        config.scope.launch {
            val change = PointInfoWidgetAllowedVisibilityChange(
                factor = MapUiFactor.EVENT_SNIPPET,
                allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.EXTENDED
            )
            val uiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
            val innerUiAction = MapUiAction.InnerUiAction.HandleMapUiAction(uiAction)
            config.innerUiActionFlow.emit(innerUiAction)
        }
    }

    private fun handleShowNearbyPage() {
        selectedPageIndexFlow.value = eventsListsPages.indexOfFirst { it.type == EventsListType.NEARBY }
    }

    private fun handleEventsListItemDeleted(postId: Long) {
        val config = config ?: return
        config.scope.launch {
            if (postId == lastFocusedEventPostId) {
                val selectedPageIndex = selectedPageIndexFlow.value
                val selectedEventsListType = eventsListsPages[selectedPageIndex].type
                val items = eventsListsItemsFlow.value[selectedEventsListType]?.items
                if (items != null) {
                    val removedItemPosition = items.indexOfFirst {
                        (it as? EventsListItem.EventItemUiModel)?.postId == postId
                    }
                    if (removedItemPosition != -1) {
                        val itemAfterRemovedPosition = removedItemPosition + 1
                        val itemAfterRemoved = items.getOrNull(itemAfterRemovedPosition)
                        val itemBeforeRemovedPosition = removedItemPosition - 1
                        val itemBeforeRemoved = items.getOrNull(itemBeforeRemovedPosition)
                        val itemToSelectWithPosition = when {
                            itemAfterRemoved is EventsListItem.EventItemUiModel -> itemAfterRemoved to itemAfterRemovedPosition
                            itemBeforeRemoved is EventsListItem.EventItemUiModel -> itemBeforeRemoved to itemBeforeRemovedPosition
                            else -> null
                        }
                        if (itemToSelectWithPosition != null) {
                            val uiEffect = MapUiEffect.SelectEventsListItem(
                                eventsListType = selectedEventsListType,
                                item = itemToSelectWithPosition.first
                            )
                            config.uiEffectsFlow.emit(uiEffect)
                            focusEventItem(itemToSelectWithPosition.first)
                            setSelectedItemPosition(
                                listType = selectedEventsListType,
                                position = itemToSelectWithPosition.second
                            )
                        } else {
                            config.uiEffectsFlow.emit(MapUiEffect.FocusMapItem(null))
                            config.uiEffectsFlow.emit(MapUiEffect.ResetGlobalMap)
                            setSelectedItemPosition(listType = selectedEventsListType, position = 0)
                        }
                        handleEventsListEmptyItemReachedAnalytics()
                    }
                }
            }
            eventsListNearbyDataDelegate?.removeItem(postId)
            eventsListMyDataDelegate?.removeItem(postId)
            eventsListArchiveDataDelegate?.removeItem(postId)
            config.uiEffectsFlow.emit(MapUiEffect.CloseEventsListItemDetails)
            config.uiEffectsFlow.emit(MapUiEffect.ResetGlobalMap)
            updateCanCreateNewEvent()
        }
    }

    private fun handleEventsListItemDetailsCloseClicked() {
        val config = config ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.CloseEventsListItemDetails)
        }
    }

    private fun handleJoinEvent(eventItem: EventsListItem.EventItemUiModel) {
        logMapEventWantToGo(eventItem.eventObject.eventPost)
        val config = config ?: return
        runCatching {
            config.scope.launch {
                joinEventUseCase.invoke(eventItem.eventId)
                eventsListMyDataDelegate?.fetchNextPage(true)
            }
        }.onFailure {
            Timber.e(it)
            config.scope.launch {
                config.uiEffectsFlow.emit(MapUiEffect.ShowErrorMessage(R.string.general_toast_error))
            }
        }
    }

    var lastLeavedEventId: Long? = null
    private fun handleLeaveEvent(eventItem: EventsListItem.EventItemUiModel) {
        val config = config ?: return
        runCatching {
            config.scope.launch {
                lastLeavedEventId = eventItem.eventId
                leaveEventUseCase.invoke(eventItem.eventId)
            }
        }.onFailure {
            Timber.e(it)
            config.scope.launch {
                config.uiEffectsFlow.emit(MapUiEffect.ShowErrorMessage(R.string.general_toast_error))
            }
        }
    }

    private fun handleNavigateToEvent(eventItem: EventsListItem.EventItemUiModel) {
        logMapEventGetTherePress(eventItem.eventObject.eventPost)
        val config = config ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.OpenEventNavigation(eventItem.eventObject.eventPost))
        }
    }

    private fun handleOpenEventPost(eventItem: EventsListItem.EventItemUiModel) {
        handleEventPostOpenAnalytics()
        val config = config ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.OpenEventsListItemDetails(eventItem.eventObject.eventPost))
            val change = PointInfoWidgetAllowedVisibilityChange(
                factor = MapUiFactor.EVENT_SNIPPET,
                allowedPointInfoWidgetVisibility = AllowedPointInfoWidgetVisibility.NONE
            )
            val uiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
            val innerUiAction = MapUiAction.InnerUiAction.HandleMapUiAction(uiAction)
            config.innerUiActionFlow.emit(innerUiAction)
        }
    }

    private fun handleShowEventHostProfile(eventItem: EventsListItem.EventItemUiModel) {
        val config = config ?: return
        val hostUserId = eventItem.eventObject.eventPost.user?.userId ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.OpenUserProfile(hostUserId))
        }
    }

    private fun handleShowEventParticipants(eventItem: EventsListItem.EventItemUiModel) {
        val config = config ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.OpenEventParticipantsList(eventItem.eventObject.eventPost))
        }
    }

    private fun handleShowEventCreator(eventItem: EventsListItem.EventItemUiModel) {
        val config = config ?: return
        eventItem.eventObject.eventPost.getUserId()?.let {
            config.scope.launch {
                config.uiEffectsFlow.emit(MapUiEffect.OpenEventCreatorAvatarProfile(it))
            }
        }
    }

    private fun handleLoadNextPage(type: EventsListType) {
        if (config?.mapBottomSheetDialogIsOpenFlow?.value.isNotTrue()) return
        when (type) {
            EventsListType.NEARBY -> eventsListNearbyDataDelegate?.fetchNextPage()
            EventsListType.MY -> eventsListMyDataDelegate?.fetchNextPage()
            EventsListType.ARCHIVE -> eventsListArchiveDataDelegate?.fetchNextPage()
        }
    }

    private fun handleCreateNewEvent() {
        val config = config ?: return
        config.scope.launch {
            config.uiEffectsFlow.emit(MapUiEffect.CloseEventsList)
            config.innerUiActionFlow.emit(MapUiAction.InnerUiAction.AddEvent)
        }
    }

    private fun handleSelectedPageChanged(index: Int) {
        config?.scope?.launch {
            updateFocusedEvent(index)
            selectedPageIndexFlow.value = index
            handleEventsListEmptyItemReachedAnalytics()
        }
    }

    private fun updateFocusedEvent(pageIndex: Int) {
        val eventsListType = eventsListsPages[pageIndex].type
        val selectedItem = eventsListsItemsFlow.value[eventsListType]
            ?.items
            ?.getOrNull(selectedItemPositionsMap.getOrDefault(eventsListType, 0))
        if (selectedItem is EventsListItem.EventItemUiModel) {
            focusEventItem(selectedItem)
            listTypeToAnimateFirstItemFocus = null
        } else {
            listTypeToAnimateFirstItemFocus = eventsListType
        }
    }

    private fun handleEventFiltersChanged(eventFiltersUpdate: EventFiltersUpdateUiModel) {
        mapEventsAnalyticsInteractor.logMapEventsListFilterClosed()
        val eventsListType = eventsListsPages[selectedPageIndexFlow.value].type
        when (eventsListType) {
            EventsListType.NEARBY -> eventsListNearbyDataDelegate?.setEventFilters(eventFiltersUpdate)
            EventsListType.MY -> eventsListMyDataDelegate?.setEventFilters(eventFiltersUpdate)
            EventsListType.ARCHIVE -> eventsListArchiveDataDelegate?.setEventFilters(eventFiltersUpdate)
        }
        listTypeToAnimateFirstItemFocus = eventsListType
    }

    private fun handleEventParticipationChanged(participationCategoryIndex: Int) {
        val participationCategory = uiMapper.mapEventParticipationCategory(participationCategoryIndex)
        val eventsListType = eventsListsPages[selectedPageIndexFlow.value].type
        when (eventsListType) {
            EventsListType.NEARBY -> Unit
            EventsListType.MY -> eventsListMyDataDelegate?.setParticipationCategory(participationCategory)
            EventsListType.ARCHIVE -> eventsListArchiveDataDelegate?.setParticipationCategory(participationCategory)
        }
        listTypeToAnimateFirstItemFocus = eventsListType
    }

    private fun handleEventsListItemSelected(item: SelectedEventsListItemUiModel) {
        setSelectedItemPosition(listType = item.listType, position = item.itemPosition)
        if (item.eventItem is EventsListItem.EventItemUiModel) {
            focusEventItem(item.eventItem)
        }
        handleEventsListEmptyItemReachedAnalytics()
    }

    private fun focusEventItem(eventItem: EventsListItem.EventItemUiModel) {
        if (eventItem.postId == lastFocusedEventPostId) return
        val config = config ?: return
        config.scope.launch {
            val uiEffect = MapUiEffect.UpdateCameraLocation(
                location = eventItem.location,
                zoom = EVENT_LIST_ITEM_ZOOM,
                yOffset = config.eventsListsYOffset,
                animate = true,
                isMyLocationActive = false
            )
            config.uiEffectsFlow.emit(uiEffect)
            config.uiEffectsFlow.emit(MapUiEffect.FocusMapItem(FocusedMapItem.Event(eventItem.eventObject)))
            lastFocusedEventPostId = eventItem.postId
        }
    }

    private fun handleEventsListsClosed() {
        val config = config ?: return
        initJob?.cancel()
        initJob = null
        config.scope.launch {
            config.mapBottomSheetDialogIsOpenFlow.value = false
//            config.uiEffectsFlow.emit(MapUiEffect.ShowMapControls)
            config.uiEffectsFlow.emit(MapUiEffect.FocusMapItem(null))
            config.uiEffectsFlow.emit(MapUiEffect.ResetGlobalMap)
        }
        canCreateNewEventFlow.value = null
        eventsListNearbyDataDelegate?.reset()
        eventsListMyDataDelegate?.reset()
        eventsListArchiveDataDelegate?.reset()
        selectedPageIndexFlow.value = 0
        listTypeToAnimateFirstItemFocus = EventsListType.NEARBY
        lastFocusedEventPostId = null
        resetSelectedPositions()
    }

    private fun handleEventsListPressed() {
        mapEventsAnalyticsInteractor.logMapEventsListPress()
        val config = config ?: return
        if (featureTogglesContainer.mapEventsFeatureToggle.isEnabled.not()) {
            config.scope.launch {
                config.uiEffectsFlow.emit(MapUiEffect.ShowEventsStubDialog)
            }
            return
        }
        val mapSettings = getMapSettingsUseCase.invoke()
        if (mapSettings.showEvents.not()) {
            config.scope.launch {
                config.uiEffectsFlow.emit(
                    MapUiEffect.ShowEnableEventsLayerDialog(EnableEventsDialogConfirmAction.OPEN_EVENTS_LIST)
                )
            }
            return
        }
        initJob = config.scope.launch {
            config.mapBottomSheetDialogIsOpenFlow.value = true
            config.uiEffectsFlow.emit(MapUiEffect.OpenEventsList)
            config.uiEffectsFlow.emit(MapUiEffect.HideMapControls)
            updateCanCreateNewEvent()
            eventsListNearbyDataDelegate?.fetchNextPage()
            eventsListMyDataDelegate?.fetchNextPage()
            eventsListArchiveDataDelegate?.fetchNextPage()
        }
    }

    private suspend fun updateCanCreateNewEvent() {
        var availableEventCount: Int? = runCatching { getAvailableMapEventCountUseCase.invoke() }
            .onFailure(Timber::e)
            .getOrNull()
        if (availableEventCount == null) {
            availableEventCount = runCatching { getAvailableMapEventCountFromLocalProfileUseCase.invoke() }
                .onFailure(Timber::e)
                .getOrNull()
        }
        val canCreateNewEvent = availableEventCount != null && availableEventCount > 0
        canCreateNewEventFlow.value = canCreateNewEvent
    }

    private fun updateEventsListsItems(eventsListType: EventsListType, items: EventsListItemsUiModel) {
        eventsListsItemsFlow.update {
            it.plus(eventsListType to items)
        }
        if (listTypeToAnimateFirstItemFocus == eventsListType) {
            val firstItem = items.items.firstOrNull()
            if (firstItem is EventsListItem.EventItemUiModel) {
                focusEventItem(firstItem)
                listTypeToAnimateFirstItemFocus = null
            }
        }
        handleEventsListEmptyItemReachedAnalytics()
    }

    private fun initializeEventsListNearbyDataDelegate(config: MapEventsListsDelegateConfigUiModel) {
        eventsListNearbyDataDelegate = EventsListNearbyDataDelegate(
            scope = config.scope,
            canCreateNewEventFlow = canCreateNewEventFlow,
            getEventsListNearbyUseCase = getEventsListNearbyUseCase,
            getDefaultEventsListFiltersNearbyUseCase = getDefaultEventsListFiltersNearbyUseCase,
            setEventsListFiltersUseCase = setEventsListFiltersUseCase,
            uiMapper = uiMapper,
            mapEventsAnalyticsInteractor = mapEventsAnalyticsInteractor
        ).apply {
            itemsFlow
                .onEach { items -> updateEventsListsItems(eventsListType = EventsListType.NEARBY, items = items) }
                .launchIn(config.scope)
        }
    }

    private fun initializeEventsListMyDataDelegate(config: MapEventsListsDelegateConfigUiModel) {
        eventsListMyDataDelegate = EventsListMyDataDelegate(
            scope = config.scope,
            canCreateNewEventFlow = canCreateNewEventFlow,
            getEventsListMyUseCase = getEventsListMyUseCase,
            getDefaultEventsListFiltersMyUseCase = getDefaultEventsListFiltersMyUseCase,
            setEventsListFiltersUseCase = setEventsListFiltersUseCase,
            uiMapper = uiMapper,
            mapEventsAnalyticsInteractor = mapEventsAnalyticsInteractor
        ).apply {
            itemsFlow
                .onEach { items -> updateEventsListsItems(eventsListType = EventsListType.MY, items = items) }
                .launchIn(config.scope)
        }
    }

    private fun initializeEventsListArchiveDataDelegate(config: MapEventsListsDelegateConfigUiModel) {
        eventsListArchiveDataDelegate = EventsListArchiveDataDelegate(
            scope = config.scope,
            canCreateNewEventFlow = canCreateNewEventFlow,
            getEventsListArchiveUseCase = getEventsListArchiveUseCase,
            getDefaultEventsListFiltersArchiveUseCase = getDefaultEventsListFiltersArchiveUseCase,
            setEventsListFiltersUseCase = setEventsListFiltersUseCase,
            uiMapper = uiMapper,
            mapEventsAnalyticsInteractor = mapEventsAnalyticsInteractor
        ).apply {
            itemsFlow
                .onEach { items -> updateEventsListsItems(eventsListType = EventsListType.ARCHIVE, items = items) }
                .launchIn(config.scope)
        }
    }

    private fun observeEventParticipationChanges(config: MapEventsListsDelegateConfigUiModel) {
        config.scope.launch {
            observeEventParticipationChangesUseCase.invoke()
                .onEach { post ->
                    val updatedEventItem = uiMapper.mapEventsItemUiModel(post) ?: return@onEach
                    handleEventParticipationChange(updatedEventItem)
                }
                .catch {
                    Timber.e(it)
                    config.uiEffectsFlow.emit(MapUiEffect.ShowErrorMessage(R.string.general_toast_error))
                }
                .launchIn(config.scope)
        }
    }

    private fun handleEventParticipationChange(updatedEventItem: EventsListItem.EventItemUiModel) {
        eventsListsItemsFlow.update { eventsListsItems ->
            if (updatedEventItem.participants.participation.isParticipant) {
                showShareDialogFlow.value = updatedEventItem.eventObject.eventPost
            }
            eventsListsItems.map { (type, itemsUiModel) ->
                val items = itemsUiModel.items.toMutableList()
                if(type == EventsListType.MY) {
                    items.removeIf { it is EventsListItem.EventItemUiModel && it.eventId == lastLeavedEventId}
                }
                val updatedItems = items
                    .map { item -> mapUpdatedEventListItem(item = item, updatedEventItem = updatedEventItem) }
                val updatedItemsUiModel = itemsUiModel.copy(items = updatedItems)
                type to updatedItemsUiModel
            }.toMap()
        }
    }

    private fun mapUpdatedEventListItem(
        item: EventsListItem,
        updatedEventItem: EventsListItem.EventItemUiModel
    ): EventsListItem =
        if ((item as? EventsListItem.EventItemUiModel)?.eventId == updatedEventItem.eventId) {
            updatedEventItem
        } else {
            item
        }

    private fun getInitialValuesCompositeFlow(): Flow<InitialValuesCompositeUiModel> = flowOf(
        InitialValuesCompositeUiModel(
            defaultFiltersMap = defaultFiltersMap,
            eventsListsPages = eventsListsPages,
        )
    )

    private fun resetSelectedPositions() {
        setSelectedItemPosition(listType = EventsListType.NEARBY, position = 0)
        setSelectedItemPosition(listType = EventsListType.MY, position = 0)
        setSelectedItemPosition(listType = EventsListType.ARCHIVE, position = 0)
    }

    private fun setSelectedItemPosition(listType: EventsListType, position: Int) {
        selectedItemPositionsMap[listType] = position
    }

    private fun handleEventsListEmptyItemReachedAnalytics() {
        val listType = eventsListsPages.getOrNull(selectedPageIndexFlow.value)?.type ?: return
        val selectedItemPosition = selectedItemPositionsMap.getOrDefault(listType, 0)
        val selectedItemIsEmptyStub = eventsListsItemsFlow.value[listType]
            ?.items
            ?.getOrNull(selectedItemPosition) is EventsListItem.EmptyItemUiModel
        val itemAfterSelectedPosition = selectedItemPosition + 1
        val itemAfterSelectedIsEmptyStub = eventsListsItemsFlow.value[listType]
            ?.items
            ?.getOrNull(itemAfterSelectedPosition) is EventsListItem.EmptyItemUiModel
        if (selectedItemIsEmptyStub || itemAfterSelectedIsEmptyStub) {
            when (listType) {
                EventsListType.NEARBY -> eventsListNearbyDataDelegate?.handleEventsListEmptyItemReachedAnalytics()
                EventsListType.MY -> eventsListMyDataDelegate?.handleEventsListEmptyItemReachedAnalytics()
                EventsListType.ARCHIVE -> eventsListArchiveDataDelegate?.handleEventsListEmptyItemReachedAnalytics()
            }
        }
    }

    private fun logMapEventGetTherePress(post: PostUIEntity) {
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = post.event?.id ?: return,
            authorId = post.user?.userId ?: return
        )
        mapEventsAnalyticsInteractor.logMapEventGetTherePress(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = AmplitudePropertyMapEventsGetThereWhere.LIST
        )
    }

    private fun logMapEventWantToGo(post: PostUIEntity) {
        val eventId = post.event?.id ?: return
        val authorId = post.user?.userId ?: return
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = eventId,
            authorId = authorId
        )
        val mapEventInvolvementParamsAnalyticsModel = MapEventInvolvementParamsAnalyticsModel(
            membersCount = post.event.participation.participantsCount,
            reactionCount = post.reactions?.size ?: 0,
            commentCount = post.commentCount
        )
        mapEventsAnalyticsInteractor.logMapEventWantToGo(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = AmplitudePropertyMapEventsWantToGoWhere.LIST,
            mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel
        )
    }

    private fun handleEventPostOpenAnalytics() {
        val selectedPage = eventsListsPages.getOrNull(selectedPageIndexFlow.value) ?: return
        val openType = when (selectedPage.type) {
            EventsListType.NEARBY -> AmplitudePropertyMapSnippetOpenType.NEARBY
            EventsListType.MY -> when (eventsListMyDataDelegate?.getFilters()?.participationCategory ?: return) {
                EventParticipationCategory.HOST -> AmplitudePropertyMapSnippetOpenType.MY_CREATOR
                EventParticipationCategory.PARTICIPANT -> AmplitudePropertyMapSnippetOpenType.MY_MEMBER
            }

            EventsListType.ARCHIVE -> when (eventsListArchiveDataDelegate?.getFilters()?.participationCategory
                ?: return) {
                EventParticipationCategory.HOST -> AmplitudePropertyMapSnippetOpenType.ARCHIVE_CREATOR
                EventParticipationCategory.PARTICIPANT -> AmplitudePropertyMapSnippetOpenType.ARCHIVE_MEMBER
            }
        }
        mapAnalyticsInteractor.logMapSnippetOpen(
            openType = openType,
            snippetType = AmplitudePropertyMapSnippetType.EVENT
        )
    }

    private data class InitialValuesCompositeUiModel(
        val defaultFiltersMap: Map<EventsListType, EventsListFiltersModel>,
        val eventsListsPages: List<EventsListPageDescription>
    )

    companion object {
        private const val DEFAULT_SELECTED_PAGE_INDEX = 0
        private const val EVENT_LIST_ITEM_ZOOM = 12f
    }
}
