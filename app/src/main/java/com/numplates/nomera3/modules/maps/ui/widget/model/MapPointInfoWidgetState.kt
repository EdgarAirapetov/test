package com.numplates.nomera3.modules.maps.ui.widget.model

sealed interface MapPointInfoWidgetState {
    object Hidden : MapPointInfoWidgetState
    sealed interface Shown : MapPointInfoWidgetState {
        data class ExtendedGeneral(
            val primaryAddress: String
        ) : Shown
        data class ExtendedDetailed(
            val primaryAddress: String,
            val secondaryAddress: String,
            val timeString: String,
            val weather: WeatherUiModel?,
            val withMeeraLogo: Boolean = false,
            val showSecondaryAddress: Boolean = true
        ) : Shown
        data class Collapsed(
            val primaryAddress: String,
            val timeString: String,
            val weather: WeatherUiModel?,
            val withMeeraLogo: Boolean = false
        ) : Shown
    }
}
