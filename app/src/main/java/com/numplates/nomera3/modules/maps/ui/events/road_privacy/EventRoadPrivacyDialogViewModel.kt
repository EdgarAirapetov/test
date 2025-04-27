package com.numplates.nomera3.modules.maps.ui.events.road_privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.mapper.EventRoadPrivacyUiMapper
import com.numplates.nomera3.modules.maps.ui.events.road_privacy.model.EventRoadPrivacyEvent
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class EventRoadPrivacyDialogViewModel @Inject constructor(
    private val mapper: EventRoadPrivacyUiMapper,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor
) : ViewModel() {

    private val roadPrivacyValueFlow = MutableStateFlow<SettingsUserTypeEnum?>(null)
    val liveUiModel = roadPrivacyValueFlow
        .filterNotNull()
        .map(mapper::mapUiModel)
        .distinctUntilChanged()
        .asLiveData()

    private val _eventFlow = MutableSharedFlow<EventRoadPrivacyEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun setRoadPrivacy(value: SettingsUserTypeEnum) {
        roadPrivacyValueFlow.value = value
    }

    fun setRoadVisibilityToAll() {
        val roadPrivacySetting = SettingsParams.PrivacySettingsParams(
            key = SettingsKeyEnum.SHOW_PERSONAL_ROAD,
            value = SettingsUserTypeEnum.ALL
        )
        setSettingsUseCase.invoke(roadPrivacySetting)
        viewModelScope.launch {
            _eventFlow.emit(EventRoadPrivacyEvent.RoadPrivacyAllIsSet)
            mapEventsAnalyticsInteractor.logSelfFeedVisibilityChangeToAll()
        }
    }
}
