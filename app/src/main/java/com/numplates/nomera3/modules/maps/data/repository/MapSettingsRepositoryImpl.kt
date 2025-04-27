package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.usersettings.PrivacySettingDto
import com.meera.db.models.usersettings.PrivacySettingsResponseDto
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.maps.domain.events.model.EventFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate
import com.numplates.nomera3.modules.maps.domain.model.MapMode
import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_ON_MAP
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * We disable themes for now as per [BR-13765](https://nomera.atlassian.net/browse/BR-13765),
 * MapMode.DAY is always returned in MapSettingsModel, get and set for mapMode should be implemented later if needed
 */
/**
 * We disable gender selection for now as per [BR-22979](https://nomera.atlassian.net/browse/BR-22979),
 * TRUE is always returned in MapSettingsModel, get and set for genders should be implemented later if needed
 */
@AppScope
class MapSettingsRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
    private val apiMain: ApiMain,
    private val dataStore: DataStore,
) : MapSettingsRepository {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val userVisibilityOnMapSettingFlow = MutableStateFlow<SettingsUserTypeEnum?>(null)

    init {
        scope.launch {
            userVisibilityOnMapSettingFlow.value = getUserVisibilityOnMapSetting()
        }
    }

    override fun getMapSettings(): MapSettingsModel {
        val filterEventDate = FilterEventDate.fromValue(appSettings.readFilterEventDate()) ?: FilterEventDate.ALL
        val filterEventTypes = appSettings
            .readFilterSettings(AppSettings.KEY_FILTER_EVENT_TYPES, EventTypesWrapperModel::class.java)
            ?.eventTypeValues
            ?.mapNotNull(EventType::fromValue)
            ?: emptyList()
        return MapSettingsModel(
            mapMode = MapMode.DAY,
            showMen = true,
            showWomen = true,
            showFriendsOnly = false,
            showPeople = appSettings.readShowPeople(),
            showEvents = appSettings.readShowEvents(),
            showFriends = appSettings.readShowFriends(),
            eventFilters = EventFiltersModel(
                eventDateFilter = filterEventDate,
                eventTypeFilter = filterEventTypes
            )
        )
    }

    override fun setMapSettings(mapSettingsModel: MapSettingsModel) {
        appSettings.writeShowFriendsOnly(mapSettingsModel.showFriendsOnly)
        appSettings.writeFilterEventDate(mapSettingsModel.eventFilters.eventDateFilter.value)
        val filterEventTypes = EventTypesWrapperModel(
            mapSettingsModel.eventFilters.eventTypeFilter.map { it.value }
        )
        appSettings.writeFilterSettings(AppSettings.KEY_FILTER_EVENT_TYPES, filterEventTypes)
        appSettings.writeShowPeople(mapSettingsModel.showPeople)
        appSettings.writeShowEvents(mapSettingsModel.showEvents)
        appSettings.writeShowFriends(mapSettingsModel.showFriends)
    }

    override fun observeUserVisibilityOnMapSetting(): Flow<SettingsUserTypeEnum> {
        return userVisibilityOnMapSettingFlow.filterNotNull()
    }

    override fun setUserVisibilityOnMapSetting(typeEnum: SettingsUserTypeEnum) {
        scope.launch {
            val requestBody =
                PrivacySettingsResponseDto(listOf(PrivacySettingDto(SHOW_ON_MAP.key, typeEnum.key)))
            try {
                apiMain.setPrivacySetting(requestBody)
                dataStore.privacySettingsDao().updateValue(SHOW_ON_MAP.key, typeEnum.key)
                userVisibilityOnMapSettingFlow.emit(typeEnum)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override suspend fun getUserVisibilityOnMapSetting(): SettingsUserTypeEnum {
        return runCatching {
            val showMeOnMapKey = dataStore.privacySettingsDao().getByKey(SHOW_ON_MAP.key).value
            SettingsUserTypeEnum.values().find { it.key == showMeOnMapKey } ?: SettingsUserTypeEnum.ALL
        }.getOrDefault(SettingsUserTypeEnum.ALL)
    }

    private data class EventTypesWrapperModel(
        val eventTypeValues: List<Int>
    )
}
