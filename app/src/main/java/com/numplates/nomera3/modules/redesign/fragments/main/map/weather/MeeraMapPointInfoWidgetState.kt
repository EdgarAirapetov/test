package com.numplates.nomera3.modules.redesign.fragments.main.map.weather

import com.numplates.nomera3.modules.maps.ui.widget.model.WeatherUiModel

sealed interface MeeraMapPointInfoWidgetState {
    object Hidden : MeeraMapPointInfoWidgetState
    sealed interface Shown : MeeraMapPointInfoWidgetState {
        data class ExtendedGeneral(
            val primaryAddress: String
        ) : Shown
        data class ExtendedDetailed(
            val primaryAddress: String,
            val secondaryAddress: String,
            val timeString: String,
            val weather: WeatherUiModel?,
        ) : Shown
        data class Collapsed(
            val primaryAddress: String,
            val timeString: String,
            val weather: WeatherUiModel?
        ) : Shown
    }
}
