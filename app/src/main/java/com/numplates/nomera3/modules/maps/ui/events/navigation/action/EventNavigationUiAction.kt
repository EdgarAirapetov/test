package com.numplates.nomera3.modules.maps.ui.events.navigation.action

import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel

sealed interface EventNavigationUiAction {
    data class OnInitialized(val initUiModel: EventNavigationInitUiModel) : EventNavigationUiAction
    sealed interface AnalyticsUiAction : EventNavigationUiAction {
        data class MapEventToNavigator(
            val appName: String
        ) : AnalyticsUiAction
    }
}
