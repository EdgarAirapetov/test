package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoUseCase
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDateChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsLastPlaceChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsOnboardingType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTimeChoice
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsTypeEvent
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.GetNewPostStreamUseCase
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventCreatedConfigurationParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetAvailableMapEventCountUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetMapEventSnippetsUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetMapEventsUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.NeedToShowEventsOnboardingUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.SetEventsOnboardingShownUseCase
import com.numplates.nomera3.modules.maps.domain.model.MapBoundsModel
import com.numplates.nomera3.modules.maps.domain.usecase.GetEventPostUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMap
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMap.Companion.EVENT_SNIPPET_PAGE_SIZE
import com.numplates.nomera3.modules.maps.ui.events.model.AddEventButtonState
import com.numplates.nomera3.modules.maps.ui.events.model.AddressSearchState
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationUiMode
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventEditingSetupUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventParametersUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventsInfoUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapCameraState
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.maps.ui.snippet.model.DataFetchingStateModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapUiFactor
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import com.numplates.nomera3.modules.places.domain.usecase.GetPlacesByLocationUseCase
import com.numplates.nomera3.modules.places.domain.usecase.SearchPlacesByTextUseCase
import com.numplates.nomera3.modules.places.ui.model.PlacesSearchUiState
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventConfigurationUiMode
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraMapUiMapper
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.TimeZone
import javax.inject.Inject

@OptIn(FlowPreview::class)
class MeeraEventsOnMapImpl @Inject constructor(
    private val uiMapper: MeeraMapUiMapper,
    private val getPlacesByLocationUseCase: GetPlacesByLocationUseCase,
    private val searchPlacesByTextUseCase: SearchPlacesByTextUseCase,
    private val uploadRepository: UploadRepository,
    private val getAvailableMapEventCountUseCase: GetAvailableMapEventCountUseCase,
    private val getMapEventsUseCase: GetMapEventsUseCase,
    private val getMapEventSnippetsUseCase: GetMapEventSnippetsUseCase,
    private val needToShowEventsOnboardingUseCase: NeedToShowEventsOnboardingUseCase,
    private val setEventsOnboardingShownUseCase: SetEventsOnboardingShownUseCase,
    private val getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val getAppInfoUseCase: GetAppInfoUseCase,
    private val getNewPostStreamUseCase: GetNewPostStreamUseCase,
    private val getEventPostUseCase: GetEventPostUseCase,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor,
    private val mapAnalyticsInteractor: MapAnalyticsInteractor,
    private val getMapSettingsUseCase: GetMapSettingsUseCase
) : EventsOnMap {
    private val isEventsOnMapEnabled get() = featureTogglesContainer.mapEventsFeatureToggle.isEnabled

    private val _liveMapEvents = MutableLiveData<List<EventObjectUiModel>>(emptyList())
    override val liveMapEvents = _liveMapEvents as LiveData<List<EventObjectUiModel>>
    private val _livePlacesSearchState = MutableLiveData<PlacesSearchUiState>()
    override val livePlacesSearchState = _livePlacesSearchState as LiveData<PlacesSearchUiState>
    private val _uiEffectsFlow = MutableSharedFlow<MapUiEffect>()
    override val uiEffectsFlow = _uiEffectsFlow as Flow<MapUiEffect>

    private val userEventCountCheckInProgressFlow = MutableStateFlow(false)
    override val liveAddEventButtonState: LiveData<AddEventButtonState> = combine(
        uploadRepository.getState()
            .map { it.status is UploadStatus.Processing }
            .onStart { emit(false) },
        userEventCountCheckInProgressFlow
    ) { uploadInProgress, eventCountCheckInProgress ->
        when {
            eventCountCheckInProgress -> AddEventButtonState.PROGRESS
            uploadInProgress -> AddEventButtonState.DISABLED
            else -> AddEventButtonState.ENABLED
        }
    }
        .asLiveData()
    override var savedUploadPostBundle: UploadPostBundle? = null

    private val innerUiActionFlow = MutableSharedFlow<MapUiAction.InnerUiAction>()
    private val eventPlaceStateFlow = MutableStateFlow<PlaceModel?>(null)
    private val eventConfigurationUiModeFlow: MutableStateFlow<MeeraEventConfigurationUiMode> =
        MutableStateFlow(MeeraEventConfigurationUiMode.EMPTY)
    private val addressSearchStateFlow = MutableStateFlow<AddressSearchState>(AddressSearchState.Default)
    private val mapCameraStateFlow = MutableStateFlow<MapCameraState>(MapCameraState.Idle)
    private val eventConfigurationMarkerStateFlow = combine(
        eventPlaceStateFlow,
        addressSearchStateFlow,
        mapCameraStateFlow,
        uiMapper::mapMarkerState
    ).distinctUntilChanged()
    val selectedEventTypeFlow = MutableStateFlow(EventType.getDefault())
    private val selectedUriFlow = MutableStateFlow<UIAttachmentPostModel?>(null)
    val selectedDateFlow = MutableStateFlow<LocalDate?>(null)
    val selectedTimeFlow = MutableStateFlow<LocalTime?>(null)

    //    private val imageUriFlow = MutableStateFlow<Uri?>(null)
    private val minimumTimeInstantFlow = MutableStateFlow(getMinimumZonedDateTime().toInstant())
    private val eventTimeFlow = combine(
        minimumTimeInstantFlow,
        selectedTimeFlow,
        selectedDateFlow,
        eventPlaceStateFlow,
//        imageUriFlow,
        uiMapper::mapEventTime
    )
    private val isMyLocationActiveFlow = MutableStateFlow(false)
    private var eventParametersUiModel: EventParametersUiModel? = null

    private var addressSearchJob: Job? = null

    private var isUserWaitingForEventPublishSuccess = false
    private var isMapOpenInTab = false

    override val liveEventConfigurationState = combine(
        eventConfigurationUiModeFlow,
        eventConfigurationMarkerStateFlow,
        selectedEventTypeFlow,
        eventTimeFlow,
        isMyLocationActiveFlow,
        uiMapper::mapEventConfigurationState
    ).distinctUntilChanged()
        .asLiveData()
    private val timePickerUiModelFlow = MutableStateFlow<TimePickerUiModel?>(null)


    private val selectedEventFlow = MutableStateFlow<EventObjectUiModel?>(null)
    private val eventPagesFlow = MutableStateFlow(mapOf<Int, List<EventObjectUiModel>>())
    private val errorStateFlow = MutableStateFlow<Throwable?>(null)
    private val loadingStateFlow = MutableStateFlow(false)
    private val dataFetchingStateFlow = combine(
        errorStateFlow,
        loadingStateFlow
    ) { error, loading ->
        DataFetchingStateModel(
            error = if (loading) null else error,
            loading = loading
        )
    }
    private val mapEventSnippetFlow = combine(
        selectedEventFlow,
        eventPagesFlow,
        dataFetchingStateFlow,
        uiMapper::mapEventSnippetUiModel
    )
    private val auxMapEventFlow = MutableStateFlow<EventObjectUiModel?>(null)
    override val liveAuxEventSnippetDataUiState = auxMapEventFlow
        .map(uiMapper::mapAuxEventSnippetUiModel)
        .distinctUntilChanged()
        .asLiveData()
    override val liveEventSnippetDataUiState = mapEventSnippetFlow
        .distinctUntilChanged()
        .debounce(EVENT_FLOW_DEBOUNCE_DURATION_MS)
        .asLiveData()

    private val scope = CoroutineScope(
        Dispatchers.Main.immediate
            + SupervisorJob()
            + CoroutineExceptionHandler { _, throwable -> Timber.e(throwable) }
    )

    private var addEventJob: Job? = null
    private var availableEventCount: Int? = null

    private val compositeDisposable = CompositeDisposable()
    private var configurationParamsAnalyticsModel: MapEventCreatedConfigurationParamsAnalyticsModel =
        getDefaultMapEventCreatedConfigurationParamsAnalyticsModel(1)

    init {
        combine(
            eventPlaceStateFlow.filterNotNull(),
            eventTimeFlow,
            selectedEventTypeFlow,
            selectedUriFlow,
            uiMapper::mapEventParametersUiModel
        ).distinctUntilChanged()
            .onEach { eventUiModel -> this.eventParametersUiModel = eventUiModel }
            .launchIn(scope)

        getNewPostStreamUseCase.invoke()
            .filter { it is PostActionModel.PostCreationSuccessModel }
            .map { it as PostActionModel.PostCreationSuccessModel }
            .filter { it.eventId != null }
            .subscribe(
                ::handleEventPostPublishingSuccess,
                Timber::e
            ).addTo(compositeDisposable)
        eventTimeFlow
            .onEach { eventTimeUiModel -> timePickerUiModelFlow.value = uiMapper.mapTimePickerModel(eventTimeUiModel) }
            .launchIn(scope)
        eventConfigurationUiModeFlow
            .onEach { eventConfigurationUiMode ->
                val visibility = if (eventConfigurationUiMode == MeeraEventConfigurationUiMode.CLOSED) {
                    AllowedPointInfoWidgetVisibility.EXTENDED
                } else {
                    AllowedPointInfoWidgetVisibility.NONE
                }
                val change = PointInfoWidgetAllowedVisibilityChange(
                    factor = MapUiFactor.EVENT_CONFIGURATION,
                    allowedPointInfoWidgetVisibility = visibility
                )
                val uiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
                val innerUiAction = MapUiAction.InnerUiAction.HandleMapUiAction(uiAction)
                scope.launch {
                    innerUiActionFlow.emit(innerUiAction)
                }
            }
            .launchIn(scope)
    }

    override fun getInnerUiActionFlow(): Flow<MapUiAction.InnerUiAction> = innerUiActionFlow

    override fun setEventSnippetState(snippetState: SnippetState) {
        val stableState = snippetState as? SnippetState.StableSnippetState ?: return
        val visibility = when (stableState) {
            SnippetState.Closed -> AllowedPointInfoWidgetVisibility.EXTENDED
            SnippetState.Expanded -> AllowedPointInfoWidgetVisibility.NONE
            SnippetState.Preview, SnippetState.HalfCollapsedPreview -> AllowedPointInfoWidgetVisibility.COLLAPSED
        }
        val change = PointInfoWidgetAllowedVisibilityChange(
            factor = MapUiFactor.EVENT_SNIPPET,
            allowedPointInfoWidgetVisibility = visibility
        )
        val uiAction = MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged(change)
        val innerUiAction = MapUiAction.InnerUiAction.HandleMapUiAction(uiAction)
        scope.launch {
            innerUiActionFlow.emit(innerUiAction)
        }
    }

    override fun setupEventEditing(setupUiModel: EventEditingSetupUiModel) {
        val eventStartInstant = uiMapper.mapEventStartInstant(setupUiModel)
        val currentMinStartTimeInstant = getMinimumZonedDateTime().toInstant()
        minimumTimeInstantFlow.value = if (eventStartInstant.isAfter(currentMinStartTimeInstant)) {
            currentMinStartTimeInstant
        } else {
            eventStartInstant
        }
        selectedEventTypeFlow.value = setupUiModel.eventType
        selectedTimeFlow.value = setupUiModel.time
        selectedDateFlow.value = setupUiModel.date
        eventPlaceStateFlow.value = setupUiModel.place
        addressSearchStateFlow.value = AddressSearchState.Success
        eventConfigurationUiModeFlow.value = MeeraEventConfigurationUiMode.OPEN
    }

    override fun onSelectedImage(uri: UIAttachmentPostModel) {
        selectedUriFlow.value = uri
    }

    override fun getEvents(visibleBounds: LatLngBounds) {
        val mapSettings = getMapSettingsUseCase.invoke()
        if (mapSettings.showEvents.not()) {
            _liveMapEvents.value = emptyList()
            return
        }
        scope.launch {
            runCatching {
                val params = MapBoundsModel(
                    southWest = CoordinatesModel(
                        lat = visibleBounds.southwest.latitude,
                        lon = visibleBounds.southwest.longitude
                    ),
                    northEast = CoordinatesModel(
                        lat = visibleBounds.northeast.latitude,
                        lon = visibleBounds.northeast.longitude
                    )
                )
                val events = withContext(Dispatchers.Default) {
                    uiMapper.mapEvents(getMapEventsUseCase.invoke(params))
                }
                _liveMapEvents.value = events
            }.onFailure(Timber::e)
        }
    }

    override fun updateMainMapEvent(post: PostUIEntity) {
        _liveMapEvents.value = _liveMapEvents.value?.let { events ->
            events.map { eventObject ->
                if (eventObject.eventPost.postId == post.postId) {
                    eventObject.copy(eventPost = post.copy(isNotExpandedSnippetState = true))
                } else {
                    eventObject
                }
            }
        }
    }

    override fun onSearchPlaces(searchText: String) {
        addressSearchJob?.cancel()
        addressSearchJob = scope.launch {
            try {
                _livePlacesSearchState.value = PlacesSearchUiState.Progress
                val places = searchPlacesByTextUseCase.invoke(searchText)
                    .filter { it.name.isNotEmpty() }
                    .map(uiMapper::mapPlaceItem)
                _livePlacesSearchState.value = if (places.isEmpty()) {
                    PlacesSearchUiState.NoResults
                } else {
                    PlacesSearchUiState.Result(places)
                }
            } catch (e: Throwable) {
                _livePlacesSearchState.value = PlacesSearchUiState.Error
                Timber.e("Search places error ${e.message}")
            }
        }
    }

    override fun onPlacesSearchCleared() {
        addressSearchJob?.cancel()
        addressSearchJob = null
        _livePlacesSearchState.value = PlacesSearchUiState.Default
    }

    override fun onEventPlaceSelected(place: PlaceModel) {
        updatePlace(place)
        addressSearchStateFlow.value = AddressSearchState.Success
        configurationParamsAnalyticsModel = configurationParamsAnalyticsModel.copy(
            writeLocationUse = true,
            lastPlaceChoice = AmplitudePropertyMapEventsLastPlaceChoice.WRITE
        )
    }

    override fun onEventPublished() {
        setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
        isUserWaitingForEventPublishSuccess = true
    }

    override fun needToShowEventsOnboarding(): Boolean = runCatching {
        if (eventConfigurationUiModeFlow.value == MeeraEventConfigurationUiMode.ONBOARDING) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                UiKitNavigationBarViewVisibilityState.GONE
        }
        needToShowEventsOnboardingUseCase() && isEventsOnMapEnabled
    }
        .getOrElse {
            Timber.e(it)
            false
        }

    override fun setEventsOnboardingShown() = runCatching { setEventsOnboardingShownUseCase() }
        .onFailure(Timber::e)

    override fun getAddress(latLng: LatLng) {
        addressSearchJob?.cancel()
        addressSearchJob = scope.launch {
            try {
                addressSearchStateFlow.value = AddressSearchState.Progress
                val location = CoordinatesModel(lat = latLng.latitude, lon = latLng.longitude)
                val place = getPlacesByLocationUseCase.invoke(location).first()
                updatePlace(place.copy(location = location))
                addressSearchStateFlow.value = AddressSearchState.Success
            } catch (t: Throwable) {
                Timber.e(t)
                delay(ERROR_DELAY_MS)
                addressSearchStateFlow.value = AddressSearchState.Error
            }
        }
    }

    override fun setMapCameraState(mapCameraState: MapCameraState) {
        if (mapCameraState is MapCameraState.Moving) {
            addressSearchStateFlow.value = AddressSearchState.Default
            isUserWaitingForEventPublishSuccess = false
            if (mapCameraState.initiatedByUser) {
                configurationParamsAnalyticsModel = configurationParamsAnalyticsModel.copy(
                    mapMoveUse = true,
                    lastPlaceChoice = AmplitudePropertyMapEventsLastPlaceChoice.MOVE
                )
            }
        }
        mapCameraStateFlow.value = mapCameraState
    }

    override fun setMapOpenInTab(isMapOpenInTab: Boolean) {
        this.isMapOpenInTab = isMapOpenInTab
    }

    override fun setEventConfigurationUiMode(eventConfigurationUiMode: EventConfigurationUiMode) = Unit

    override fun setEventConfigurationUiMode(eventConfigurationUiMode: MeeraEventConfigurationUiMode) {
        when {
            eventConfigurationUiMode == MeeraEventConfigurationUiMode.OPEN &&
                eventConfigurationUiModeFlow.value == MeeraEventConfigurationUiMode.CLOSED -> {
                val timeZone = eventPlaceStateFlow.value?.timeZone ?: TimeZone.getDefault()
                val currentZonedDateTime = minimumTimeInstantFlow.value
                    .atZone(timeZone.toZoneId())
                val currentTime = currentZonedDateTime.toLocalTime()
                savedUploadPostBundle = null
                val minimumZonedDateTime = getMinimumZonedDateTime()
                selectedDateFlow.value = minimumZonedDateTime.toLocalDate()
                selectedTimeFlow.value = currentTime
                minimumTimeInstantFlow.value = minimumZonedDateTime.toInstant()
                selectedEventTypeFlow.value = EventType.getDefault()
            }

            eventConfigurationUiMode is MeeraEventConfigurationUiMode.STEP2_FINISHED -> {
//                savedUploadPostBundle = null
//                val minimumZonedDateTime = getMinimumZonedDateTime()
//                selectedDateFlow.value = minimumZonedDateTime.toLocalDate()
//                selectedTimeFlow.value = minimumZonedDateTime.toLocalTime()
//                minimumTimeInstantFlow.value = minimumZonedDateTime.toInstant()
//                selectedEventTypeFlow.value = EventType.getDefault()
//                eventPlaceStateFlow.value = null
            }

            eventConfigurationUiMode == MeeraEventConfigurationUiMode.FIRST_STEP -> {
                savedUploadPostBundle = null
                val minimumZonedDateTime = getMinimumZonedDateTime()
                selectedDateFlow.value = minimumZonedDateTime.toLocalDate()
                selectedTimeFlow.value = minimumZonedDateTime.toLocalTime()
                minimumTimeInstantFlow.value = minimumZonedDateTime.toInstant()
                selectedEventTypeFlow.value = EventType.getDefault()
                eventPlaceStateFlow.value = null
            }

            eventConfigurationUiMode == MeeraEventConfigurationUiMode.OPEN &&
                eventConfigurationUiModeFlow.value == MeeraEventConfigurationUiMode.ONBOARDING -> {
                scope.launch {
                    runCatching {
                        availableEventCount = getAvailableMapEventCountUseCase.invoke()
                    }.onFailure(Timber::e)
                }
            }
        }
        eventConfigurationUiModeFlow.value = eventConfigurationUiMode
    }

    override fun setSelectedEventType(eventTypeItemUiModel: EventTypeItemUiModel) {
        selectedEventTypeFlow.value = eventTypeItemUiModel.type
    }

    override fun setSelectedEventDate(eventDateItemUiModel: EventDateItemUiModel) {
        val timeZone = eventPlaceStateFlow.value?.timeZone ?: TimeZone.getDefault()
        val minimumZonedDateTime = minimumTimeInstantFlow.value
            .atZone(timeZone.toZoneId())
        val minimumDate = minimumZonedDateTime.toLocalDate()
        val minimumTime = minimumZonedDateTime.toLocalTime()
        val date = eventDateItemUiModel.date
        val time = selectedTimeFlow.value
        val eventDate = if (date.isBefore(minimumDate)) {
            minimumDate
        } else {
            date
        }
        val eventTime = if (
            time == null
            || (date == minimumDate && time.isBefore(minimumTime))
            || (date.isBefore(minimumDate))
        ) {
            minimumTime
        } else {
            time
        }
        selectedDateFlow.value = eventDate
        selectedTimeFlow.value = eventTime
        configurationParamsAnalyticsModel = configurationParamsAnalyticsModel.copy(
            dateChoice = AmplitudePropertyMapEventsDateChoice.CUSTOM
        )
    }

    override fun setSelectedTime(time: LocalTime) = Unit

    override fun setSelectedTimeWithRes(time: LocalTime): LocalTime {
        if (time == selectedTimeFlow.value) return time
        val resultTime = uiMapper.mapEventTime(
            minimumTimeInstantFlow.value,
            time,
            selectedDateFlow.value,
            eventPlaceStateFlow.value
        )

        selectedTimeFlow.value = resultTime.time
        configurationParamsAnalyticsModel = configurationParamsAnalyticsModel.copy(
            timeChoice = AmplitudePropertyMapEventsTimeChoice.CUSTOM
        )
        return resultTime.time
    }

    override fun setMyLocationActive(isActive: Boolean) {
        isMyLocationActiveFlow.value = isActive
        configurationParamsAnalyticsModel = configurationParamsAnalyticsModel.copy(
            findMeUse = true,
            lastPlaceChoice = AmplitudePropertyMapEventsLastPlaceChoice.FIND_ME
        )
    }

    override fun onSelectTime() {
        timePickerUiModelFlow.value?.let { uiModel ->
            scope.launch {
                val event = MapUiEffect.ShowEventTimePicker(uiModel)
                _uiEffectsFlow.emit(event)
            }
        }
    }

    override fun onSelectAddress() {
        scope.launch {
            val searchText = eventPlaceStateFlow.value
                ?.name
                ?: String.empty()
            val event = MapUiEffect.ShowAddressSearch(searchText)
            _uiEffectsFlow.emit(event)
        }
    }

    override fun onShowEventsAbout() {
        scope.launch {
            val event = MapUiEffect.ShowEventsAbout(EventsInfoUiModel(availableEventCount))
            _uiEffectsFlow.emit(event)
        }
    }

    override fun getEventParameters(): EventParametersUiModel? {
        return eventParametersUiModel
    }

    override fun addEvent() {
        if (featureTogglesContainer.mapEventsFeatureToggle.isEnabled.not()) {
            scope.launch {
                _uiEffectsFlow.emit(MapUiEffect.ShowCreateEventStubDialog)
            }
            return
        }
        val mapSettings = getMapSettingsUseCase.invoke()
        if (mapSettings.showEvents.not()) {
            scope.launch {
                _uiEffectsFlow.emit(
                    MapUiEffect.ShowEnableEventsLayerDialog(EnableEventsDialogConfirmAction.CREATE_EVENT)
                )
            }
            return
        }
        if (userEventCountCheckInProgressFlow.compareAndSet(expect = false, update = true)) {
            addEventJob?.cancel()
            addEventJob = scope.launch {
                try {
                    if (getAppInfoUseCase.invoke().appInfo.isEmpty()) return@launch
                    val availableEventCount = getAvailableMapEventCountUseCase.invoke()
                    this@MeeraEventsOnMapImpl.availableEventCount = availableEventCount
                    if (availableEventCount > 0) {
                        configurationParamsAnalyticsModel = getDefaultMapEventCreatedConfigurationParamsAnalyticsModel(
                            EventConstants.MAX_USER_EVENT_COUNT - availableEventCount + 1
                        )
                        _uiEffectsFlow.emit(MapUiEffect.ShowEventConfigurationUi)
                    } else {
                        _uiEffectsFlow.emit(MapUiEffect.ShowEventLimitReached)
                    }
                } finally {
                    userEventCountCheckInProgressFlow.value = false
                }
            }
        }
    }

    override fun cancelAddEvent() {
        addEventJob?.cancel()
        addEventJob = null
        setEventConfigurationUiMode(MeeraEventConfigurationUiMode.CLOSED)
    }

    override fun setSelectedEvent(eventObject: EventObjectUiModel?) {
        selectedEventFlow.value = eventObject
        eventPagesFlow.value = mapOf()
        if (eventObject != null) {
            scope.launch {
                eventObject.eventPost.event?.let { event ->
                    doGetNextEventsPage(event)
                }
            }
        }
    }

    override fun setSelectedEvent(event: PostUIEntity) {
        val eventObject = uiMapper.mapEventObjectUiModel(event)
        selectedEventFlow.value = eventObject
        eventPagesFlow.value = mapOf()
        if (eventObject != null) {
            scope.launch {
                eventObject.eventPost.event?.let { event ->
                    doGetNextEventsPage(event)
                }
            }
        }
    }

    override fun getNextEventSnippetPage() {
        scope.launch {
            selectedEventFlow.value?.eventPost?.event?.let { doGetNextEventsPage(it) }
        }
    }

    override fun setAuxMapEventSelected(eventObject: EventObjectUiModel?) {
        auxMapEventFlow.value = eventObject
    }

    override fun updateAuxMapEvent(eventObject: EventObjectUiModel) {
        _liveMapEvents.value = listOf(eventObject)
    }

    override fun getSettings(): Settings? = getAppInfoAsyncUseCase.executeBlocking()

    override fun clear() {
        scope.cancel()
        compositeDisposable.clear()
    }

    override fun logMapEventSnippetOpen(openType: AmplitudePropertyMapSnippetOpenType) =
        mapAnalyticsInteractor.logMapSnippetOpen(
            openType = openType,
            snippetType = AmplitudePropertyMapSnippetType.EVENT
        )

    override fun setMapEventSnippetCloseMethod(closeMethod: MapSnippetCloseMethod) {
        mapAnalyticsInteractor.setMapSnippetCloseMethod(closeMethod)
    }

    override fun logMapEventSnippetClosed() {
        mapAnalyticsInteractor.logMapSnippetClose(AmplitudePropertyMapSnippetType.EVENT)
    }

    override fun logMapEventOnboardingAction(
        onboardingType: AmplitudePropertyMapEventsOnboardingType,
        actionType: AmplitudePropertyMapEventsOnboardingActionType
    ) {
        val typeEvent = if (onboardingType == AmplitudePropertyMapEventsOnboardingType.FIRST) {
            uiMapper.mapAmplitudePropertyMapEventsTypeEvent(selectedEventTypeFlow.value)
        } else {
            AmplitudePropertyMapEventsTypeEvent.NONE
        }
        val defaultTypeEvent = if (onboardingType == AmplitudePropertyMapEventsOnboardingType.FIRST) {
            uiMapper.mapAmplitudePropertyMapEventsTypeEvent(EventType.getDefault())
        } else {
            AmplitudePropertyMapEventsTypeEvent.NONE
        }
        mapEventsAnalyticsInteractor
            .logMapEventOnboardingAction(
                actionType = actionType,
                onboardingType = onboardingType,
                typeEvent = typeEvent,
                defaultTypeEvent = defaultTypeEvent
            )
    }

    private suspend fun doGetNextEventsPage(selectedEvent: EventUiModel) {
        if (!loadingStateFlow.compareAndSet(expect = false, update = true)) return
        val pages = eventPagesFlow.value
        try {
            val lastPageSize = pages.entries
                .maxByOrNull { it.key }
                ?.value
                ?.size
            if (lastPageSize == 0) return
            val excludedEventIds = pages.values.flatten().mapNotNull { it.eventPost.event?.id }
            val pageIndex = pages.size
            val params = GetMapEventSnippetsParamsModel(
                selectedEventId = selectedEvent.id,
                excludedEventIds = excludedEventIds,
                location = selectedEvent.address.location.let { CoordinatesModel(it.latitude, it.longitude) },
                limit = EVENT_SNIPPET_PAGE_SIZE
            )
            val events = withContext(Dispatchers.Default) {
                uiMapper.mapEvents(getMapEventSnippetsUseCase.invoke(params))
            }
            if (selectedEventFlow.value?.eventPost?.event?.id == selectedEvent.id) {
                eventPagesFlow.value = eventPagesFlow.value.plus(
                    pageIndex to events
                )
            }
        } catch (t: Throwable) {
            Timber.e(t)
            val isFirstPage = pages.isEmpty()
            if (isFirstPage.not()) {
                delay(LOADER_ANIMATION_DELAY_MS)
            }
            errorStateFlow.value = t
        } finally {
            loadingStateFlow.value = false
        }
    }

    private fun handleEventPostPublishingSuccess(postCreationSuccess: PostActionModel.PostCreationSuccessModel) {
        scope.launch {
            runCatching {
                val eventObject = uiMapper.mapEvent(getEventPostUseCase.invoke(postCreationSuccess.postId))
                    ?: return@launch
                logMapEventCreated(eventObject.eventPost)
                if (isUserWaitingForEventPublishSuccess && isMapOpenInTab) {
                    _uiEffectsFlow.emit(MapUiEffect.FocusMapItem(FocusedMapItem.Event(eventObject)))
                    setSelectedEvent(eventObject)
                } else {
                    _uiEffectsFlow.emit(MapUiEffect.UpdateEventsOnMap)
                }
            }
        }
    }

    private fun getMinimumZonedDateTime(): ZonedDateTime =
        ZonedDateTime.now()
            .withNano(0)
            .withSecond(0)

    private fun updatePlace(place: PlaceModel) {
        val oldTimeZone = eventPlaceStateFlow.value?.timeZone ?: TimeZone.getDefault()
        val newTimeZone = place.timeZone
        if (oldTimeZone != newTimeZone) {
            updateSelectedDateTime(oldTimeZone = oldTimeZone, newTimeZone = newTimeZone)
        }
        eventPlaceStateFlow.value = place
    }

    private fun updateSelectedDateTime(oldTimeZone: TimeZone, newTimeZone: TimeZone) {
        val oldTime = selectedTimeFlow.value ?: return
        val oldDate = selectedDateFlow.value ?: return
        val zonedDateTime = ZonedDateTime
            .of(oldDate, oldTime, oldTimeZone.toZoneId())
            .withZoneSameInstant(newTimeZone.toZoneId())
        selectedTimeFlow.value = zonedDateTime.toLocalTime()
        selectedDateFlow.value = zonedDateTime.toLocalDate()
    }

    private fun logMapEventCreated(post: PostUIEntity) {
        val mapEventCreatedEventParamsAnalyticsModel = uiMapper.mapMapEventCreatedEventParamsAnalyticsModel(post)
            ?: return
        mapEventsAnalyticsInteractor.logMapEventCreated(
            mapEventCreatedConfigurationParamsAnalyticsModel = configurationParamsAnalyticsModel,
            mapEventCreatedEventParamsAnalyticsModel = mapEventCreatedEventParamsAnalyticsModel
        )
    }

    private fun getDefaultMapEventCreatedConfigurationParamsAnalyticsModel(
        eventNumber: Int
    ): MapEventCreatedConfigurationParamsAnalyticsModel = MapEventCreatedConfigurationParamsAnalyticsModel(
        mapMoveUse = false,
        findMeUse = false,
        writeLocationUse = false,
        lastPlaceChoice = AmplitudePropertyMapEventsLastPlaceChoice.MOVE,
        dateChoice = AmplitudePropertyMapEventsDateChoice.DEFAULT,
        timeChoice = AmplitudePropertyMapEventsTimeChoice.DEFAULT,
        defaultTypeEvent = uiMapper.mapAmplitudePropertyMapEventsTypeEvent(EventType.getDefault()),
        eventNumber = eventNumber
    )

    companion object {
        private const val EVENT_FLOW_DEBOUNCE_DURATION_MS = 200L
        private const val ERROR_DELAY_MS = 500L
        private const val LOADER_ANIMATION_DELAY_MS = 1000L
    }
}
