package com.numplates.nomera3.modules.maps.ui.events.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.action.EventNavigationUiAction
import com.numplates.nomera3.modules.maps.ui.events.navigation.mapper.EventNavigationUiMapper
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationUiModel
import javax.inject.Inject

class EventNavigationDialogViewModel @Inject constructor(
    private val uiMapper: EventNavigationUiMapper,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor
)  : ViewModel() {

    private val _liveUiModel = MutableLiveData<EventNavigationUiModel>()
    val liveUiModel = _liveUiModel as LiveData<EventNavigationUiModel>

    private var initUiModel: EventNavigationInitUiModel? = null

    fun handleUiAction(uiAction: EventNavigationUiAction) {
        when (uiAction) {
            is EventNavigationUiAction.OnInitialized -> {
                initUiModel = uiAction.initUiModel
                _liveUiModel.value = uiMapper.mapUiModel(uiAction.initUiModel.event)
            }
            is EventNavigationUiAction.AnalyticsUiAction.MapEventToNavigator -> logMapEventToNavigator(uiAction.appName)
        }
    }

    private fun logMapEventToNavigator(appName: String) {
        val initUiModel = this.initUiModel ?: return
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = initUiModel.event.id,
            authorId = initUiModel.authorId
        )
        mapEventsAnalyticsInteractor.logMapEventToNavigator(
            geoServiceName = uiMapper.mapAmplitudePropertyMapEventsGeoServiceName(appName),
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel
        )
    }
}
