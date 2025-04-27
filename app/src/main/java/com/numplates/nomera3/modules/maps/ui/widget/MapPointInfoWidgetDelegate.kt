package com.numplates.nomera3.modules.maps.ui.widget

import com.numplates.nomera3.modules.maps.domain.widget.model.GetMapWidgetPointInfoParamsModel
import com.numplates.nomera3.modules.maps.domain.widget.usecase.GetMapWidgetPointInfoUseCase
import com.numplates.nomera3.modules.maps.ui.model.MapUiAction
import com.numplates.nomera3.modules.maps.ui.widget.mapper.MapPointInfoWidgetUiMapper
import com.numplates.nomera3.modules.maps.ui.widget.model.AllowedPointInfoWidgetVisibility
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoWidgetDelegateConfigUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapTargetUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.MapUiFactor
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoExtendedUiModel
import com.numplates.nomera3.modules.maps.ui.widget.model.PointInfoWidgetAllowedVisibilityChange
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class MapPointInfoWidgetDelegate @Inject constructor(
    private val getMapWidgetPointInfoUseCase: GetMapWidgetPointInfoUseCase,
    private val uiMapper: MapPointInfoWidgetUiMapper,
    private val networkStatusProvider: NetworkStatusProvider,
) {
    private var config: MapPointInfoWidgetDelegateConfigUiModel? = null
    private val allowedPointInfoWidgetVisibilityFlow = MutableStateFlow(AllowedPointInfoWidgetVisibility.NONE)
    private val visibilityFactorsMap = mutableMapOf<MapUiFactor, AllowedPointInfoWidgetVisibility>()
    private val isNetworkConnected = MutableStateFlow<Boolean>(true)
    private val pointInfoWidgetModelFlow = MutableStateFlow<PointInfoExtendedUiModel?>(null)
    val uiModel = combine(
        pointInfoWidgetModelFlow,
        allowedPointInfoWidgetVisibilityFlow,
        createTimeTickerFlow(),
            isNetworkConnected,
    ) { pointInfoExtended, visibility, timestampMs, isConntected ->
        uiMapper.mapUiModel(
            pointInfoExtended = pointInfoExtended,
            visibility = visibility,
            timestampMs = timestampMs,
            isConntected = isConntected,
        )
    }
        .distinctUntilChanged()

    private var pointInfoJob: Job? = null
    private var lastMapTarget: MapTargetUiModel? = null

    init {
        visibilityFactorsMap[MapUiFactor.MAP_TAB] = AllowedPointInfoWidgetVisibility.NONE
    }

    fun initialize(config: MapPointInfoWidgetDelegateConfigUiModel) {
        this.config = config
    }

    fun handleEventsListsUiAction(uiAction: MapUiAction.MapWidgetPointInfoUiAction) {
        when (uiAction) {
            is MapUiAction.MapWidgetPointInfoUiAction.MapTargetChanged ->
                handleMapTargetChanged(uiAction.mapTarget)

            is MapUiAction.MapWidgetPointInfoUiAction.MapUiStateChanged ->
                handleMapUiStateChanged(uiAction.change)

            is MapUiAction.MapWidgetPointInfoUiAction.WithMeeraLogo -> Unit
            MapUiAction.MapWidgetPointInfoUiAction.RefreshMapPoint -> Unit
        }
    }

    private fun createTimeTickerFlow(): Flow<Long> =
        flow {
            while (coroutineContext.isActive) {
                val now = ZonedDateTime.now()
                emit(now.toInstant().toEpochMilli())
                val nextMinute = now.plusMinutes(1).withSecond(0).withNano(0)
                val delayToNextMinuteMs = Duration.between(now, nextMinute).toMillis()
                delay(delayToNextMinuteMs)
            }
        }

    private fun handleMapUiStateChanged(change: PointInfoWidgetAllowedVisibilityChange) {
        visibilityFactorsMap[change.factor] = change.allowedPointInfoWidgetVisibility
        allowedPointInfoWidgetVisibilityFlow.value = visibilityFactorsMap.values.minBy { it.priority }
    }

    private fun handleMapTargetChanged(mapTarget: MapTargetUiModel) {
        if (mapTarget == lastMapTarget) return
        lastMapTarget = mapTarget
        val config = config ?: return
        pointInfoJob?.cancel()
        pointInfoJob = config.scope.launch {
            runCatching {
                isNetworkConnected.value = networkStatusProvider.isInternetConnected()

                val params = GetMapWidgetPointInfoParamsModel(
                    latitude = mapTarget.latLng.latitude,
                    longitude = mapTarget.latLng.longitude,
                    getWeather = uiMapper.mapGetWeather(mapTarget.zoom)
                )
                val mapWidgetPointInfo = getMapWidgetPointInfoUseCase.invoke(params)
                val pointInfoExtended = PointInfoExtendedUiModel(
                    pointInfo = mapWidgetPointInfo,
                    mapTarget = mapTarget,
                    isNetworkConnected = networkStatusProvider.isInternetConnected()
                )
                pointInfoWidgetModelFlow.value = pointInfoExtended
            }.onFailure { throwable ->
                lastMapTarget = null
                Timber.e(throwable)
            }
        }
    }
}
