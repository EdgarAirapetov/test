package com.numplates.nomera3.presentation.viewmodel

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.LocationUtility
import com.meera.db.models.UploadType
import com.numplates.nomera3.data.network.core.ResponseError
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.domain.exception.ResponseException
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapfriends.AmplitudeMapFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.model.GetMapObjectsParamsModel
import com.numplates.nomera3.modules.maps.domain.usecase.GetCurrentLocationUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetDefaultMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetEventPostUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapObjectsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetUserVisibilityOnMapUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.NeedToShowGeoPopupUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ObserveUserLocationUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ObserveUserVisibilityOnMapUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ReadLastLocationFromStorageUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ResetGeoPopupShownCountUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetGeoPopupShownUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.ui.MapParametersCache
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMap
import com.numplates.nomera3.modules.maps.ui.events.EventsOnMapImpl
import com.numplates.nomera3.modules.maps.ui.events.list.delegate.MapEventsListsDelegate
import com.numplates.nomera3.modules.maps.ui.events.list.model.MapEventsListsDelegateConfigUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationState
import com.numplates.nomera3.modules.maps.ui.events.model.EventSnippetDataUiState
import com.numplates.nomera3.modules.maps.ui.friends.MapFriendsListsDelegate
import com.numplates.nomera3.modules.maps.ui.friends.MapFriendsListsDelegateConfigUiModel
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupAction
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.GeoPopupOrigin
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.toAmplitudePropertyGeoPopupActionType
import com.numplates.nomera3.modules.maps.ui.geo_popup.model.toAmplitudePropertyGeoPopupWhere
import com.numplates.nomera3.modules.maps.ui.layers.model.EnableEventsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.mapper.MapUiMapper
import com.numplates.nomera3.modules.maps.ui.model.EventObjectUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.model.MapObjectsUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.maps.ui.model.MapUiState
import com.numplates.nomera3.modules.maps.ui.model.MapUiValuesUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.widget.MapPointInfoWidgetDelegate
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetDelegateConfigUiModel
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.notifications.ui.viewmodel.SingleLiveEvent
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle
import com.numplates.nomera3.modules.upload.mapper.UploadBundleMapper
import com.numplates.nomera3.modules.upload.util.getUploadBundle
import com.numplates.nomera3.modules.user.domain.usecase.AvatarChangesObserverUseCase
import com.numplates.nomera3.modules.user.domain.usecase.AvatarObserverParams
import com.numplates.nomera3.modules.user.domain.usecase.GetUserAccountColorUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserAccountTypeUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserAvatarUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserGenderUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.UpdateOwnUserProfileUseCase
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.telecom.CallUiEventDispatcher
import com.numplates.nomera3.telecom.model.CallUiEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val mapAnalyticsInteractor: MapAnalyticsInteractor,
    private val mapParametersCache: MapParametersCache,
    private val getMapSettingsUseCase: GetMapSettingsUseCase,
    private val observeUserVisibilityOnMapUseCase: ObserveUserVisibilityOnMapUseCase,
    private val uiMapper: MapUiMapper,
    private val avatarObserver: AvatarChangesObserverUseCase,
    private val getMapObjectsUseCase: GetMapObjectsUseCase,
    private val needToShowGeoPopupUseCase: NeedToShowGeoPopupUseCase,
    private val setGeoPopupShownUseCase: SetGeoPopupShownUseCase,
    private val resetGeoPopupShownCountUseCase: ResetGeoPopupShownCountUseCase,
    private val callUiEventDispatcher: CallUiEventDispatcher,
    private val getUserVisibilityOnMapUseCase: GetUserVisibilityOnMapUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val readLastLocationFromStorageUseCase: ReadLastLocationFromStorageUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val eventsOnMapImpl: EventsOnMapImpl,
    private val getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase,
    private val observeLocalOwnUserProfileModelUseCase: ObserveLocalOwnUserProfileModelUseCase,
    private val context: Context,
    private val getEventPostUseCase: GetEventPostUseCase,
    private val getUserAccountTypeUseCase: GetUserAccountTypeUseCase,
    private val getUserAccountColorUseCase: GetUserAccountColorUseCase,
    private val getUserGenderUseCase: GetUserGenderUseCase,
    private val getUserAvatarUseCase: GetUserAvatarUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor,
    private val getDefaultMapSettingsUseCase: GetDefaultMapSettingsUseCase,
    private val setMapSettingsUseCase: SetMapSettingsUseCase,
    private val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase,
    private val updateOwnUserProfileUseCase: UpdateOwnUserProfileUseCase,
    private val mapEventsListsDelegate: MapEventsListsDelegate,
    private val mapFriendsListsDelegate: MapFriendsListsDelegate,
    private val amplitudeMapFriends: AmplitudeMapFriends,
    private val mapPointInfoWidgetDelegate: MapPointInfoWidgetDelegate
) : ViewModel() {

    val isEventsOnMapEnabled get() = featureTogglesContainer.mapEventsFeatureToggle.isEnabled
    val isFriendsOnMapEnabled get() = featureTogglesContainer.mapFriendsFeatureToggle.isEnabled
    val eventsOnMap: EventsOnMap = eventsOnMapImpl

    private val disposables = CompositeDisposable()

    private val _liveMapObjects = MutableLiveData<MapObjectsUiModel>()
    val liveMapObjects = _liveMapObjects as LiveData<MapObjectsUiModel>
    private val _liveErrorEvent = SingleLiveEvent<ResponseError>()
    val liveErrorEvent = _liveErrorEvent as LiveData<ResponseError>

    private val userSnippetStateFlow = MutableStateFlow<SnippetState>(SnippetState.Closed)
    private val mapBottomSheetDialogIsOpenFlow = MutableStateFlow(false)

    private val googleMapInitializedFlow = MutableStateFlow(false)
    private val mapUiValuesFlow = MutableStateFlow<MapUiValuesUiModel?>(null)
    private val mapModeFlow = MutableStateFlow<MapMode?>(null)

    val liveNavBarOpen = combine(
        userSnippetStateFlow,
        eventsOnMap.liveEventConfigurationState.asFlow(),
        eventsOnMap.liveEventSnippetDataUiState.asFlow(),
        mapBottomSheetDialogIsOpenFlow
    ) { snippetState, eventConfigurationState, eventSnippetUiModel, mapBottomSheetDialogIsOpen ->
        snippetState == SnippetState.Closed
            && eventConfigurationState == EventConfigurationState.Closed
            && eventSnippetUiModel is EventSnippetDataUiState.Empty
            && mapBottomSheetDialogIsOpen.not()
    }.asLiveData()

    val eventsListUiModel = mapEventsListsDelegate.uiModel.asLiveData()
    val friendListUiEffect = mapFriendsListsDelegate.uiEffectsFlow.asLiveData()
    val mapFriendsListUiModel = mapFriendsListsDelegate.uiModelStateFlow.asLiveData()

    val pointInfoWidgetUiModel = mapPointInfoWidgetDelegate.uiModel.asLiveData()

    fun getMapParametersCache(): MapParametersCache = mapParametersCache

    fun readLastLocation() = readLastLocationFromStorageUseCase.invoke()?.let(uiMapper::mapLatLng)

    fun readAccountType() = getUserAccountTypeUseCase.invoke()

    fun readAccountColor() = getUserAccountColorUseCase.invoke()

    fun getUserUid() = getUserUidUseCase.invoke()

    fun getAvatarMarker(): String? {
        myMarkerImage = getUserAvatarUseCase.invoke()
        return myMarkerImage
    }

    fun getGender(): Gender = getUserGenderUseCase.invoke()

    private val _uiStateFlow = MutableStateFlow<MapUiState?>(null)
    val uiStateFlow = _uiStateFlow.filterNotNull()
    private val _uiEffectsFlow = MutableSharedFlow<MapUiEffect>()
    val uiEffectsFlow = merge(_uiEffectsFlow, eventsOnMap.uiEffectsFlow)
    private val nonDefaultMapSettingsFlow = MutableStateFlow(areMapSettingsNonDefault())
    private val innerUiActionFlow = MutableSharedFlow<MapUiAction.InnerUiAction>()
    private val mergedInnerUiActionFlow = merge(innerUiActionFlow, eventsOnMap.getInnerUiActionFlow())
    private var getMapObjectsJob: Job? = null

    private var isShowMeOnMapEnabled: Boolean? = null
    private var myMarkerImage: String? = null
    private var locationPermissionGranted: Boolean? = null

    init {
        combine(
            googleMapInitializedFlow.filter { isGoogleMapInitialized -> isGoogleMapInitialized },
            mapModeFlow.filterNotNull(),
            mapUiValuesFlow.filterNotNull(),
            nonDefaultMapSettingsFlow,
        ) { _, mapMode, mapUiValues, nonDefaultMapSettings ->
            uiMapper.mapUiState(
                mapMode = mapMode,
                mapUiValues = mapUiValues,
                nonDefaultLayersSettings = nonDefaultMapSettings
            )
        }
            .onEach { uiState ->
                if (_uiStateFlow.value == null) {
                    _uiEffectsFlow.emit(MapUiEffect.InitializeUi(uiState))
                }
                _uiStateFlow.value = uiState
            }
            .launchIn(viewModelScope)
        observeUserVisibilityOnMapUseCase.invoke()
            .onEach { createMyMarker() }
            .launchIn(viewModelScope)
        callUiEventDispatcher.eventFlow()
            .onEach { callUiState ->
                val callUiVisible = callUiState == CallUiEvent.CREATED
                _uiEffectsFlow.emit(MapUiEffect.CallUiStateChanged(callUiVisible))
            }
            .launchIn(viewModelScope)
        observeUserLocationUseCase.invoke()
            .onEach { createMyMarker() }
            .launchIn(viewModelScope)
        subscribeMomentsEventsUseCase.invoke()
            .mapNotNull { it as? MomentRepositoryEvent.UserMomentsStateUpdated }
            .onEach(::handleUserMomentsStateUpdated)
            .launchIn(viewModelScope)
        mergedInnerUiActionFlow
            .onEach(::handleInnerUiAction)
            .launchIn(viewModelScope)
    }

    val mapSettings
        get() = getMapSettingsUseCase.invoke()

    override fun onCleared() {
        super.onCleared()
        eventsOnMap.clear()
        disposables.clear()
    }

    fun setupEventEditing() {
        (mapModeFlow.value as? MapMode.EventEditing)?.eventEditingSetupUiModel?.let {
            eventsOnMap.setupEventEditing(it)
        }
    }

    fun enableFriendLayer() {
        handleUiAction(MapFriendsListUiAction.EnableFriendLayer)
    }

    fun createEventObjectFromEventPost(eventPost: PostUIEntity): EventObjectUiModel? =
        uiMapper.mapEventObjectUiModel(eventPost)

    fun observeAvatarChanges() {
        val disposable = avatarObserver.execute(AvatarObserverParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.avatarSmall?.let { _ ->
                    createMyMarker()
                }
            }, {
                Timber.e(it)
            })
        disposables.add(disposable)
    }

    fun getMapObjects(
        gpsXMin: Double,
        gpsXMax: Double,
        gpsYMin: Double,
        gpsYMax: Double,
        zoom: Double
    ) {
        val mapSettings = getMapSettingsUseCase.invoke()
        if (mapSettings.showFriends.not() && mapSettings.showPeople.not()) {
            val emptyMapObjects = MapObjectsUiModel(
                users = emptyList(),
                clusters = emptyList(),
                nearestFriend = null
            )
            _liveMapObjects.postValue(emptyMapObjects)
            return
        }
        getMapObjectsJob?.cancel()
        getMapObjectsJob = viewModelScope.launch {
            val params = GetMapObjectsParamsModel(
                gpsXMin = gpsXMin,
                gpsXMax = gpsXMax,
                gpsYMin = gpsYMin,
                gpsYMax = gpsYMax,
                zoom = zoom
            )
            try {
                val mapObjects = getMapObjectsUseCase.invoke(params)
                _liveMapObjects.postValue(uiMapper.mapMapObjects(mapObjects))
            } catch (t: Throwable) {
                (t as? ResponseException)?.responseError
                    ?.let(_liveErrorEvent::postValue)
                Timber.e(t)
            }
        }
    }

    fun needToShowGeoPopup(): Boolean = runCatching { needToShowGeoPopupUseCase.invoke() }
        .getOrElse {
            Timber.e(it)
            false
        }

    fun setGeoPopupShown() {
        runCatching { setGeoPopupShownUseCase.invoke() }
            .onFailure(Timber::e)
    }

    fun resetGeoPopupShownCount() = runCatching { resetGeoPopupShownCountUseCase.invoke() }
        .onFailure(Timber::e)

    fun logBackToMyPinClicked() {
        mapAnalyticsInteractor.logBackToMyLocation()
    }

    fun setMapSnippetCloseMethod(method: MapSnippetCloseMethod) = mapAnalyticsInteractor.setMapSnippetCloseMethod(method)

    fun logGeoPopupAction(action: GeoPopupAction, origin: GeoPopupOrigin) {
        mapAnalyticsInteractor.logGeoPopupAction(
            actionType = action.toAmplitudePropertyGeoPopupActionType(),
            where = origin.toAmplitudePropertyGeoPopupWhere()
        )
    }

    fun setUserSnippetState(snippetState: SnippetState) {
        userSnippetStateFlow.value = snippetState
    }

    fun handleUiAction(uiAction: MapFriendsListUiAction) {
        mapFriendsListsDelegate.handleUiAction(uiAction)
    }

    fun handleUiAction(uiAction: MapUiAction) {
        when (uiAction) {
            is MapUiAction.EventsListUiAction -> mapEventsListsDelegate.handleEventsListsUiAction(uiAction)
            is MapUiAction.AnalyticsUiAction -> handleAnalyticsUiAction(uiAction)
            is MapUiAction.CreateMyMarkerRequested -> createMyMarker(
                location = uiAction.location,
                fallbackToDefaultLocation = uiAction.fallbackToDefaultLocation
            )
            is MapUiAction.ShowFriendAndUserCityBoundsRequested -> showFriendAndUserCityBounds(uiAction)
            is MapUiAction.SetCameraToUserCityLocationRequested -> goToUserCityOrDefaultLocation(uiAction.fallbackToDefault)
            is MapUiAction.MapWidgetPointInfoUiAction -> {
                mapPointInfoWidgetDelegate.handleEventsListsUiAction(uiAction)
                if(uiAction is MapUiAction.MapWidgetPointInfoUiAction.MapTargetChanged){
                    mapEventsListsDelegate.handleEventsListsUiAction(MapUiAction.EventsListUiAction.CameraChanged(uiAction.mapTarget.latLng))
                }
            }
            MapUiAction.FindMyLocationRequested -> findMyLocation()
            MapUiAction.GoogleMapInitialized -> googleMapInitializedFlow.value = true
            is MapUiAction.MapViewCreated -> parseMapMode(uiAction.arguments)
            is MapUiAction.MapUiValuesCalculated -> mapUiValuesFlow.value = uiAction.mapUiValues
            MapUiAction.MainModeInitialized -> handleMapModeMainInitialized()
            is MapUiAction.AuxMapEventUpdated -> eventsOnMap.updateAuxMapEvent(uiAction.eventObject)
            is MapUiAction.MainMapEventUpdated -> eventsOnMap.updateMainMapEvent(uiAction.eventObject.eventPost)
            is MapUiAction.OnResumeCalled -> checkLocationPermission(uiAction.isMapOpenInTab)
            is MapUiAction.MapBottomSheetDialogStateChanged -> handleMapLayersDialogStateChange(uiAction.isOpen)
            MapUiAction.FriendsListPressed -> handleFriendsListPressed()
            is MapUiAction.EnableEventsLayerDialogClosed -> handleEnableEventsLayerDialogClosed(
                enableLayerRequested = uiAction.enableLayerRequested,
                confirmAction = uiAction.confirmAction
            )
            MapUiAction.FriendsListStubDialogClosed -> handleMapDialogClosed()
            MapUiAction.MapDialogClosed -> handleMapDialogClosed()
            is MapUiAction.RemoveMediaEvent -> Unit
        }
    }

    private fun handleFriendsListPressed() {
        amplitudeMapFriends.onFriendsListPress(getUserUid())
        mapFriendsListsDelegate.handleUiAction(MapFriendsListUiAction.OpenFriendList)
    }

    private suspend fun handleUserMomentsStateUpdated(
        userMomentsStateUpdated: MomentRepositoryEvent.UserMomentsStateUpdated
    ) {
        val update = userMomentsStateUpdated.userMomentsStateUpdate
        if (update.userId == getUserUid()) {
            createMyMarker(userMomentsStateUpdate = update)
            viewModelScope.launch {
                runCatching { updateOwnUserProfileUseCase.invoke() }
                    .onFailure(Timber::e)
            }
        } else {
            val uiEffect = MapUiEffect.UpdateUserMarkerMoments(update)
            _uiEffectsFlow.emit(uiEffect)
        }
    }

    private fun handleAnalyticsUiAction(analyticsUiAction: MapUiAction.AnalyticsUiAction) {
        when (analyticsUiAction) {
            is MapUiAction.AnalyticsUiAction.MapEventCreateTap -> mapEventsAnalyticsInteractor
                .logMapEventCreateEventTap(analyticsUiAction.where)
            MapUiAction.AnalyticsUiAction.MapEventLimitAlert -> mapEventsAnalyticsInteractor
                .logMapEventLimitAlert()
            is MapUiAction.AnalyticsUiAction.MapEventOnboardingAction -> eventsOnMap.logMapEventOnboardingAction(
                onboardingType = analyticsUiAction.onboardingType,
                actionType = analyticsUiAction.actionType
            )
            MapUiAction.AnalyticsUiAction.RulesOpen -> mapEventsAnalyticsInteractor.logOpenRules()
            MapUiAction.AnalyticsUiAction.EventSnippetOpenTap -> mapAnalyticsInteractor.logMapSnippetOpen(
                openType = AmplitudePropertyMapSnippetOpenType.TAP,
                snippetType = AmplitudePropertyMapSnippetType.EVENT
            )
        }
    }

    private fun handleInnerUiAction(innerUiAction: MapUiAction.InnerUiAction) {
        when (innerUiAction) {
            MapUiAction.InnerUiAction.AddEvent -> eventsOnMap.addEvent()
            is MapUiAction.InnerUiAction.HandleMapUiAction -> handleUiAction(innerUiAction.action)
        }
    }

    private fun handleMapModeMainInitialized() {
        moveCameraToUpdatedUserLocation()
        initializeMapEventsListsDelegate()
        initializeMapFriendsListsDelegate()
        initializePointWidgetInfoDelegate()
    }

    private fun initializePointWidgetInfoDelegate() {
        val config = MapPointInfoWidgetDelegateConfigUiModel(
            uiEffectsFlow = _uiEffectsFlow,
            scope = viewModelScope,
        )
        mapPointInfoWidgetDelegate.initialize(config)
    }


    private fun initializeMapFriendsListsDelegate() {
        val config = MapFriendsListsDelegateConfigUiModel(
            uiEffectsFlow = _uiEffectsFlow,
            innerUiActionFlow = innerUiActionFlow,
            mapBottomSheetDialogIsOpenFlow = mapBottomSheetDialogIsOpenFlow,
            scope = viewModelScope,
            eventsListsYOffset = mapUiValuesFlow.value?.eventsListsYOffset ?: 0
        )
        mapFriendsListsDelegate.initialize(config)
    }
    private fun initializeMapEventsListsDelegate() {
        val config = MapEventsListsDelegateConfigUiModel(
            uiEffectsFlow = _uiEffectsFlow,
            innerUiActionFlow = innerUiActionFlow,
            mapBottomSheetDialogIsOpenFlow = mapBottomSheetDialogIsOpenFlow,
            scope = viewModelScope,
            eventsListsYOffset = mapUiValuesFlow.value?.eventsListsYOffset ?: 0
        )
        mapEventsListsDelegate.initialize(config)
    }

    private fun handleEnableEventsLayerDialogClosed(
        enableLayerRequested: Boolean,
        confirmAction: EnableEventsDialogConfirmAction
    ) {
        if (enableLayerRequested) {
            setMapSettingsUseCase.invoke(mapSettings.copy(showEvents = true))
            checkMapLayersSettings()
            viewModelScope.launch {
                _uiEffectsFlow.emit(MapUiEffect.ResetGlobalMap)
            }
            handleEnableEventsDialogConfirmAction(confirmAction)
        }
    }

    private fun handleEnableEventsDialogConfirmAction(confirmAction: EnableEventsDialogConfirmAction) {
        when (confirmAction) {
            EnableEventsDialogConfirmAction.CREATE_EVENT -> eventsOnMap.addEvent()
            EnableEventsDialogConfirmAction.OPEN_EVENTS_LIST ->
                mapEventsListsDelegate.handleEventsListsUiAction(MapUiAction.EventsListUiAction.EventsListPressed)
        }
    }

    private fun handleMapDialogClosed() {
        mapBottomSheetDialogIsOpenFlow.value = false
        viewModelScope.launch {
            _uiEffectsFlow.emit(MapUiEffect.ShowMapControls)
        }
    }

    private fun parseMapMode(args: Bundle?) {
        viewModelScope.launch {
            val userModel = args?.get(IArgContainer.ARG_USER_MODEL) as? MapUserUiModel
            val uploadPostBundleString = args?.getUploadBundle()
            val eventPost = args?.getParcelable<PostUIEntity>(MapFragment.ARG_EVENT_POST)
            val eventPostId = args?.getLong(MapFragment.ARG_EVENT_POST_ID, -1L) ?: -1L
            val mapMode = when {
                eventPostId != -1L -> {
                    val eventObject = runCatching {
                        uiMapper.mapEvent(getEventPostUseCase.invoke(eventPostId))
                    }.getOrNull()
                    MapMode.EventView(eventObject)
                }
                eventPost != null -> MapMode.EventView(createEventObjectFromEventPost(eventPost))
                userModel != null -> MapMode.UserView(user = userModel, isMe = getUserUid() == userModel.id)
                uploadPostBundleString != null -> {
                    val uploadPostBundle = (UploadBundleMapper.map(UploadType.EventPost, uploadPostBundleString) as? UploadPostBundle)
                        ?: error("Could not map UploadPostBundle")
                    val event = uploadPostBundle.event ?: error("Event is empty in UploadPostBundle")
                    val setupModel = uiMapper.mapEventEditingSetupUiModel(event)
                    MapMode.EventEditing(setupModel)
                }
                else -> MapMode.Main
            }
            (args?.getSerializable(MapFragment.ARG_LOG_MAP_OPEN_WHERE) as? AmplitudePropertyWhereOpenMap)
                ?.let(mapAnalyticsInteractor::logOpenMap)
            mapModeFlow.value = mapMode
            _uiEffectsFlow.emit(MapUiEffect.CalculateMapUiValues(mapMode))
        }
    }

    private fun moveCameraToUpdatedUserLocation() {
        viewModelScope.launch {
            var isMyLocationActive = false
            val initialLocation = if (LocationUtility.isLocationAvailable(context).not()) {
                getUserCityOrDefaultLocation(true)
            } else {
                runCatching {
                    getCurrentLocationUseCase.invoke()?.let(uiMapper::mapLatLng)
                        .also { isMyLocationActive = true }
                }.getOrNull()
            }
            if (initialLocation != null) {
                val uiEffect = MapUiEffect.UpdateCameraLocation(
                    location = initialLocation,
                    zoom = MapFragment.MAP_ZOOM_DEFAULT,
                    yOffset = 0,
                    animate = false,
                    isMyLocationActive = isMyLocationActive
                )
                _uiEffectsFlow.emit(uiEffect)
            } else {
                _uiEffectsFlow.emit(MapUiEffect.UpdateMyMarker)
            }
        }
    }

    private fun createMyMarker(
        location: LatLng? = null,
        fallbackToDefaultLocation: Boolean = false,
        resetCurrentLocationActive: Boolean = false,
        userMomentsStateUpdate: UserMomentsStateUpdateModel? = null
    ) {
        if ((mapModeFlow.value as? MapMode.UserView)?.isMe.isTrue()) return
        viewModelScope.launch {
            val latLng = location
                ?: getCurrentLocationUseCase.invoke()?.let(uiMapper::mapLatLng)
                ?: if (fallbackToDefaultLocation) getDefaultLocation() else return@launch
            val showMeOnMap = isShowMeOnMapEnabled()
            val isAvatarValid = getUserAvatarUseCase.invoke() == myMarkerImage.orEmpty()
            val markerIsObsolete = userMomentsStateUpdate != null || isShowMeOnMapEnabled != showMeOnMap || !isAvatarValid
            isShowMeOnMapEnabled = showMeOnMap
            val moments = if (userMomentsStateUpdate != null) {
                PinMomentsUiModel(
                    hasMoments = userMomentsStateUpdate.hasMoments,
                    hasNewMoments = userMomentsStateUpdate.hasNewMoments
                )
            } else {
                val userMoments = getOwnLocalProfileUseCase.invoke()?.moments
                PinMomentsUiModel(
                    hasMoments = userMoments?.hasMoments.isTrue(),
                    hasNewMoments = userMoments?.hasNewMoments.isTrue()
                )
            }
            val uiEffect = MapUiEffect.CreateMyMarker(
                latLng = latLng,
                isShowMeOnMapEnabled = showMeOnMap,
                markerIsObsolete = markerIsObsolete,
                checkCurrentLocationActive = resetCurrentLocationActive,
                moments = moments
            )
            _uiEffectsFlow.emit(uiEffect)
        }
    }

    private fun showFriendAndUserCityBounds(uiAction: MapUiAction.ShowFriendAndUserCityBoundsRequested) {
        viewModelScope.launch {
            val userProfile = getOwnLocalProfileUseCase.invoke()
            val cityName = userProfile?.coordinates?.cityName
            val userCityLocation = cityName?.let { getUserCityLocation(cityName) }
            if (userCityLocation != null) {
                val uiEffect = MapUiEffect.ShowFriendAndUserCityBounds(
                    friendLocation = uiAction.friendLocation,
                    userCityLocation = userCityLocation,
                    cameraPosition = uiAction.cameraPosition
                )
                _uiEffectsFlow.emit(uiEffect)
            }
        }
    }

    private fun goToUserCityOrDefaultLocation(fallbackToDefault: Boolean) {
        viewModelScope.launch {
            val location = getUserCityOrDefaultLocation(fallbackToDefault)
            if (location != null) {
                val uiEffect = MapUiEffect.UpdateCameraLocation(
                    location = location,
                    zoom = MapFragment.MAP_ZOOM_DEFAULT,
                    yOffset = 0,
                    animate = false,
                    isMyLocationActive = false
                )
                _uiEffectsFlow.emit(uiEffect)
            }
        }
    }

    private suspend fun getUserCityOrDefaultLocation(fallbackToDefault: Boolean): LatLng? = runCatching {
        val user = observeLocalOwnUserProfileModelUseCase.invoke().first()
        val cityName = user.coordinates?.cityName
        when {
            cityName != null -> getLocationForCityName(cityName = cityName, fallbackToDefault = fallbackToDefault)
            fallbackToDefault -> getDefaultLocation()
            else -> null
        }
    }.getOrNull()

    private fun findMyLocation() {
        viewModelScope.launch {
            runCatching {
                val currentLocation = getCurrentLocationUseCase.invoke() ?: return@runCatching
                val uiEffect = MapUiEffect.SetMyLocation(
                    location = uiMapper.mapLatLng(currentLocation),
                )
                _uiEffectsFlow.emit(uiEffect)
            }.onFailure(Timber::e)
        }
    }

    private suspend fun getLocationForCityName(cityName: String, fallbackToDefault: Boolean): LatLng? =
        getUserCityLocation(cityName) ?: if (fallbackToDefault) getDefaultLocation() else null

    private suspend fun getUserCityLocation(cityName: String): LatLng? {
        return try {
            val decoder = Geocoder(context, Locale.getDefault())
            withContext(Dispatchers.IO) {
                decoder.getFromLocationName(cityName, 2)
                    ?.firstOrNull()
                    ?.let { location -> LatLng(location.latitude, location.longitude) }
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun checkLocationPermission(isMapOpenInTab: Boolean) {
        val permissionGranted = locationPermissionGranted.isFalse() && LocationUtility.checkPermissionLocation(context)
        if (permissionGranted && mapModeFlow.value is MapMode.Main && isMapOpenInTab.not()) {
            moveCameraToUpdatedUserLocation()
        }
        locationPermissionGranted = LocationUtility.checkPermissionLocation(context)
    }

    private suspend fun isShowMeOnMapEnabled(): Boolean {
        return getUserVisibilityOnMapUseCase.invoke()
            .let { value -> value == SettingsUserTypeEnum.ALL || value == SettingsUserTypeEnum.FRIENDS }
    }

    private fun handleMapLayersDialogStateChange(isOpen: Boolean) {
        if (isOpen.not()) {
            checkMapLayersSettings()
        }
        mapBottomSheetDialogIsOpenFlow.value = isOpen
    }

    private fun checkMapLayersSettings() {
        runCatching {
            nonDefaultMapSettingsFlow.value = areMapSettingsNonDefault()
        }.onFailure(Timber::e)
    }

    private fun areMapSettingsNonDefault(): Boolean =
        getMapSettingsUseCase.invoke() != getDefaultMapSettingsUseCase.invoke()

    private fun getDefaultLocation() = LatLng(AppSettings.LOCATION_DEFAULT_LATITUDE, AppSettings.LOCATION_DEFAULT_LONGITUDE)
}
