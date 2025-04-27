package com.numplates.nomera3.modules.maps.ui.layers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters.AmplitudeMapFilters
import com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters.AmplitudePropertyMapFiltersVisibility
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.maps.domain.interactor.MapLayersTooltipsInteractor
import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.modules.maps.domain.usecase.GetDefaultMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetUserVisibilityOnMapUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetUserVisibilityOnMapUseCase
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.modules.maps.ui.entity.toAmplitudePropertyWhereMapPrivacy
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel
import com.numplates.nomera3.modules.maps.ui.layers.mapper.MapLayersUiMapper
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersTooltip
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersUiEffect
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.model.enums.toAmplitudePropertySettingVisibility
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapLayersDialogViewModel @Inject constructor(
    private val analyticsInteractor: AnalyticsInteractor,
    private val getMapSettingsUseCase: GetMapSettingsUseCase,
    private val setMapSettingsUseCase: SetMapSettingsUseCase,
    private val setUserVisibilityOnMapUseCase: SetUserVisibilityOnMapUseCase,
    private val getUserVisibilityOnMapUseCase: GetUserVisibilityOnMapUseCase,
    private val observeLocalOwnUserProfileUseCase: ObserveLocalOwnUserProfileModelUseCase,
    private val getDefaultMapSettingsUseCase: GetDefaultMapSettingsUseCase,
    private val mapper: MapLayersUiMapper,
    private val amplitudeMapFilters: AmplitudeMapFilters,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val mapLayersTooltipsInteractor: MapLayersTooltipsInteractor
) : ViewModel() {

    private var initialMapSettings: MapLayersStateModel? = null
    private val mapSettingsFlow = MutableStateFlow<MapSettingsModel?>(null)
    private val userVisibilityOnMapFlow = MutableStateFlow<SettingsUserTypeEnum?>(null)
    private val locationAvailableFlow = MutableStateFlow<Boolean?>(null)
    private val defaultUserSettings = getDefaultMapSettingsUseCase.invoke()
    private val initialDataFlow = combine(
        observeLocalOwnUserProfileUseCase.invoke().take(1),
        flowOf(defaultUserSettings),
        flowOf(featureTogglesContainer.mapEventsFeatureToggle.isEnabled)
    ) { profile, defaultMapSettings, isEventsEnabled ->
        InitialDataCompositeUiModel(
            profile = profile,
            defaultMapSettings = defaultMapSettings,
            isEventsOnMapEnabled = isEventsEnabled
        )
    }
    val liveUiModel = combine(
        initialDataFlow,
        mapSettingsFlow.filterNotNull(),
        userVisibilityOnMapFlow.filterNotNull(),
        locationAvailableFlow.filterNotNull(),
    ) { initialData, mapSettings, userVisibilityOnMap, locationAvailable ->
        mapper.mapLayersUiModel(
            userProfile = initialData.profile,
            mapSettings = mapSettings,
            userVisibilityOnMap = userVisibilityOnMap,
            locationAvailable = locationAvailable,
            defaultMapSettings = initialData.defaultMapSettings,
            isEventsEnabled = initialData.isEventsOnMapEnabled
        )
    }.distinctUntilChanged()
        .asLiveData()

    private val _uiEffectsFlow = MutableSharedFlow<MapLayersUiEffect>()
    val uiEffectsFlow = _uiEffectsFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            initialMapSettings = MapLayersStateModel(
                mapSettings = getMapSettingsUseCase.invoke(),
                showUserOnMapSetting = getUserVisibilityOnMapUseCase.invoke()
            )
            mapSettingsFlow.value = initialMapSettings?.mapSettings
            userVisibilityOnMapFlow.value = initialMapSettings?.showUserOnMapSetting
        }
        viewModelScope.launch {
            delay(TOOLTIP_ONBOARDING_INITIAL_DELAY_MS)
            if (mapLayersTooltipsInteractor.needToShowLayersTooltipOnboarding()) {
                sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(MapLayersTooltip.ONBOARDING))
                mapLayersTooltipsInteractor.writeLayersTooltipOnboardingShown()
            }
        }
    }

    fun setLocationAvailable(locationAvailable: Boolean) {
        locationAvailableFlow.value = locationAvailable
    }

    fun setShowPeopleEnabled(enabled: Boolean) {
        updateMapSettings(mapSettingsFlow.value?.copy(showPeople = enabled))
        showLayerChangeTooltips(if (enabled) null else DisabledLayer.PEOPLE)
    }

    fun setShowEventsEnabled(enabled: Boolean) {
        updateMapSettings(mapSettingsFlow.value?.copy(showEvents = enabled))
        showLayerChangeTooltips(if (enabled) null else DisabledLayer.EVENTS)
    }

    fun setShowFriendsEnabled(enabled: Boolean) {
        updateMapSettings(mapSettingsFlow.value?.copy(showFriends = enabled))
        showLayerChangeTooltips(if (enabled) null else DisabledLayer.FRIENDS)
    }

    fun setUserVisibilityOnMap(visibilityTypeIndex: Int) {
        updateUserVisibilityOnMap(mapper.mapSelectedUserVisibilityOnMapSetting(visibilityTypeIndex))
    }

    fun setEventFilterType(eventFilterType: EventFilterTypeUiModel) {
        val mapSettings = mapSettingsFlow.value ?: return
        val updatedMapSettings = mapSettings.copy(
            eventFilters = mapSettings.eventFilters.copy(
                eventTypeFilter = eventFilterType.selectedEventTypes,
            )
        )
        updateMapSettings(updatedMapSettings)
    }

    fun setEventFilterDate(eventFilterDate: EventFilterDateUiModel) {
        val mapSettings = mapSettingsFlow.value ?: return
        val updatedMapSettings = mapSettings.copy(
            eventFilters = mapSettings.eventFilters.copy(
                eventDateFilter = eventFilterDate.selectedFilterEventDate,
            )
        )
        updateMapSettings(updatedMapSettings)
    }

    fun resetEventSettingsToDefaults() {
        val mapSettings = mapSettingsFlow.value
        updateMapSettings(mapSettings?.copy(eventFilters = defaultUserSettings.eventFilters))
    }

    fun applyUserVisibilityOnMapSetting() {
        userVisibilityOnMapFlow.value?.let(setUserVisibilityOnMapUseCase::invoke)
    }

    fun logFilterApply() {
        val initialMapSettings = initialMapSettings ?: return
        val settingsChanged = initialMapSettings.mapSettings != mapSettingsFlow.value
            || initialMapSettings.showUserOnMapSetting != userVisibilityOnMapFlow.value
        amplitudeMapFilters.logMapFilterApply(settingsChanged)
    }

    fun logFiltersClosed() {
        val initialMapSettings = initialMapSettings ?: return
        val currentSettings = mapSettingsFlow.value ?: return
        val userVisibility = userVisibilityOnMapFlow.value ?: return
        val settingsChanged = initialMapSettings.mapSettings != currentSettings
            || initialMapSettings.showUserOnMapSetting != userVisibility
        val visibility = when (userVisibility) {
            SettingsUserTypeEnum.NOBODY -> AmplitudePropertyMapFiltersVisibility.NOBODY
            SettingsUserTypeEnum.ALL -> AmplitudePropertyMapFiltersVisibility.ALL
            SettingsUserTypeEnum.FRIENDS -> AmplitudePropertyMapFiltersVisibility.FRIENDS
        }
        amplitudeMapFilters.onMapFiltersClosed(
            haveChanges = settingsChanged,
            visibility = visibility,
            peopleEnabled = currentSettings.showPeople,
            eventsEnabled = currentSettings.showEvents,
            friendsEnabled = currentSettings.showFriends
        )
    }

    private fun showLayerChangeTooltips(disabledLayer: DisabledLayer?) {
        val settings = mapSettingsFlow.value ?: return
        val disabledLayersCount = listOf(settings.showPeople, settings.showEvents, settings.showFriends)
            .count { isEnabled -> isEnabled.not() }
        when {
            needToShowAllLayersDisabledTooltip(
                disabledLayersCount = disabledLayersCount,
                disabledLayer = disabledLayer
            ) -> showAllLayersDisabledTooltip()
            needToShowTwoLayersDisabledTooltip(
                disabledLayersCount = disabledLayersCount,
                disabledLayer = disabledLayer
            ) -> showTwoLayersDisabledTooltip(settings)
            needToShowPeopleLayerDisabledTooltip(
                settings = settings,
                disabledLayer = disabledLayer
            ) -> showPeopleLayersDisabledTooltip()
            needToShowEventsLayerDisabledTooltip(
                settings = settings,
                disabledLayer = disabledLayer
            ) -> showEventsLayersDisabledTooltip()
            needToShowFriendsLayerDisabledTooltip(
                settings = settings,
                disabledLayer = disabledLayer
            ) -> showFriendsLayersDisabledTooltip()
        }
    }

    private fun needToShowAllLayersDisabledTooltip(disabledLayersCount: Int, disabledLayer: DisabledLayer?): Boolean =
        disabledLayersCount == ALL_LAYERS_COUNT
            && disabledLayer != null
            && mapLayersTooltipsInteractor.needToShowLayersTooltipAllLayersDisabled()

    private fun needToShowTwoLayersDisabledTooltip(disabledLayersCount: Int, disabledLayer: DisabledLayer?): Boolean =
        disabledLayersCount == TWO_LAYERS_COUNT
            && disabledLayer != null
            && mapLayersTooltipsInteractor.needToShowLayersTooltipTwoLayersDisabled()

    private fun needToShowPeopleLayerDisabledTooltip(
        settings: MapSettingsModel,
        disabledLayer: DisabledLayer?
    ): Boolean = settings.showPeople.not()
        && disabledLayer == DisabledLayer.PEOPLE
        && mapLayersTooltipsInteractor.needToShowLayersTooltipPeopleDisabled()

    private fun needToShowEventsLayerDisabledTooltip(
        settings: MapSettingsModel,
        disabledLayer: DisabledLayer?
    ): Boolean = settings.showEvents.not()
        && disabledLayer == DisabledLayer.EVENTS
        && mapLayersTooltipsInteractor.needToShowLayersTooltipEventsDisabled()

    private fun needToShowFriendsLayerDisabledTooltip(
        settings: MapSettingsModel,
        disabledLayer: DisabledLayer?
    ): Boolean = settings.showFriends.not()
        && disabledLayer == DisabledLayer.FRIENDS
        && mapLayersTooltipsInteractor.needToShowLayersTooltipFriendsDisabled()

    private fun showAllLayersDisabledTooltip() {
        sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(MapLayersTooltip.ALL_DISABLED))
        mapLayersTooltipsInteractor.writeLayersTooltipAllLayersDisabledShown()
    }

    private fun showTwoLayersDisabledTooltip(settings: MapSettingsModel) {
        val tooltip = when {
            settings.showPeople.not() && settings.showEvents.not() -> MapLayersTooltip.PEOPLE_EVENTS_DISABLED
            settings.showPeople.not() && settings.showFriends.not() -> MapLayersTooltip.PEOPLE_FRIENDS_DISABLED
            settings.showEvents.not() && settings.showFriends.not() -> MapLayersTooltip.EVENTS_FRIENDS_DISABLED
            else -> null
        }
        tooltip?.let {
            sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(tooltip))
            mapLayersTooltipsInteractor.writeLayersTooltipTwoLayersDisabledShown()
        }
    }

    private fun showPeopleLayersDisabledTooltip() {
        sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(MapLayersTooltip.PEOPLE_DISABLED))
        mapLayersTooltipsInteractor.writeLayersTooltipPeopleDisabledShown()
    }

    private fun showEventsLayersDisabledTooltip() {
        sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(MapLayersTooltip.EVENTS_DISABLED))
        mapLayersTooltipsInteractor.writeLayersTooltipEventsDisabledShown()
    }

    private fun showFriendsLayersDisabledTooltip() {
        sendUiEffect(MapLayersUiEffect.ShowLayersTooltip(MapLayersTooltip.FRIENDS_DISABLED))
        mapLayersTooltipsInteractor.writeLayersTooltipFriendsDisabledShown()
    }

    private fun sendUiEffect(uiEffect: MapLayersUiEffect) {
        viewModelScope.launch { _uiEffectsFlow.emit(uiEffect) }
    }

    private fun updateUserVisibilityOnMap(userVisibilityOnMapSetting: SettingsUserTypeEnum) {
        if (userVisibilityOnMapFlow.value != userVisibilityOnMapSetting) {
            userVisibilityOnMapFlow.value = userVisibilityOnMapSetting
            logShowOnMapSetting(userVisibilityOnMapSetting)
        }
    }

    private fun updateMapSettings(mapSettings: MapSettingsModel?) {
        mapSettings ?: return
        if (mapSettingsFlow.value != mapSettings) {
            mapSettingsFlow.value = mapSettings
            setMapSettingsUseCase.invoke(mapSettings)
        }
    }

    private fun logShowOnMapSetting(typeEnum: SettingsUserTypeEnum) {
        analyticsInteractor.logMapPrivacySettingsSetup(
            where = MapVisibilitySettingsOrigin.MAP.toAmplitudePropertyWhereMapPrivacy(),
            visibility = typeEnum.toAmplitudePropertySettingVisibility()
        )
    }

    private data class MapLayersStateModel(
        val mapSettings: MapSettingsModel,
        val showUserOnMapSetting: SettingsUserTypeEnum
    )

    private enum class DisabledLayer {
        PEOPLE,
        EVENTS,
        FRIENDS
    }

    private data class InitialDataCompositeUiModel(
        val profile: UserProfileModel,
        val defaultMapSettings: MapSettingsModel,
        val isEventsOnMapEnabled: Boolean
    )

    companion object {
        private const val ALL_LAYERS_COUNT = 3
        private const val TWO_LAYERS_COUNT = 2
        private const val TOOLTIP_ONBOARDING_INITIAL_DELAY_MS = 500L
    }
}
