package com.numplates.nomera3.modules.maps.ui.layers.mapper

import androidx.annotation.IdRes
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersUiModel
import com.numplates.nomera3.modules.maps.ui.layers.model.MapLayersUserUiModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class MapLayersUiMapper @Inject constructor() {

    fun mapLayersUiModel(
        userProfile: UserProfileModel,
        mapSettings: MapSettingsModel,
        userVisibilityOnMap: SettingsUserTypeEnum,
        locationAvailable: Boolean,
        defaultMapSettings: MapSettingsModel,
        isEventsEnabled: Boolean
    ): MapLayersUiModel {
        val user = mapUser(userProfile)
        val userpicConfig = UserpicUiModel(userAvatarUrl = user.avatarUrl, userAvatarShow = true)
        val showNonDefaultEventSettings =
            mapSettings.eventFilters != defaultMapSettings.eventFilters
        val eventFilterType = EventFilterTypeUiModel(
            selectedEventTypes = mapSettings.eventFilters.eventTypeFilter,
        )
        val eventFilterDate = EventFilterDateUiModel(
            selectedFilterEventDate = mapSettings.eventFilters.eventDateFilter
        )
        return MapLayersUiModel(
            showPeople = mapSettings.showPeople,
            showEvents = mapSettings.showEvents,
            showFriends = mapSettings.showFriends,
            eventFilterType = eventFilterType,
            eventFilterDate = eventFilterDate,
            selectedUserVisibilityOnMapTypeIndex = mapSelectedUserVisibilityOnMapTypeIndex(userVisibilityOnMap),
            showEnableLocationStub = !locationAvailable,
            userpicConfig = userpicConfig,
            showNonDefaultEventSettings = showNonDefaultEventSettings,
            isEventsEnabled = isEventsEnabled
        )
    }

    fun mapUser(user: UserProfileModel): MapLayersUserUiModel {
        return MapLayersUserUiModel(
            accountType = createAccountTypeEnum(user.accountType),
            accountColor = user.accountColor ?: 0,
            avatarUrl = user.avatarSmall
        )
    }

    fun mapSelectedUserVisibilityOnMapSetting(visibilityTypeIndex: Int): SettingsUserTypeEnum =
        viewIdToUserVisibilityOnMapSettingMap.getOrDefault(visibilityTypeIndex, SettingsUserTypeEnum.ALL)

    @IdRes
    private fun mapSelectedUserVisibilityOnMapTypeIndex(setting: SettingsUserTypeEnum): Int =
        viewIdToUserVisibilityOnMapSettingMap.entries
            .associate { it.value to it.key }
            .getOrDefault(setting, USER_VISIBILITY_ON_MAP_TYPE_INDEX_ALL)

    companion object {
        private const val USER_VISIBILITY_ON_MAP_TYPE_INDEX_ALL = 0
        private const val USER_VISIBILITY_ON_MAP_TYPE_INDEX_FRIEND = 1
        private const val USER_VISIBILITY_ON_MAP_TYPE_INDEX_NOBODY = 2

        private val viewIdToUserVisibilityOnMapSettingMap = mapOf(
            USER_VISIBILITY_ON_MAP_TYPE_INDEX_ALL to SettingsUserTypeEnum.ALL,
            USER_VISIBILITY_ON_MAP_TYPE_INDEX_FRIEND to SettingsUserTypeEnum.FRIENDS,
            USER_VISIBILITY_ON_MAP_TYPE_INDEX_NOBODY to SettingsUserTypeEnum.NOBODY,
        )
    }
}
