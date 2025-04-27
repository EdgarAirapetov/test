package com.numplates.nomera3.modules.maps.ui.model

data class MapUiValuesUiModel(
    val mapBottomPadding: Int,
    val userSnippetYOffset: Int,
    val eventMarkerYOffset: Int,
    val eventsListsYOffset: Int,
    val mapHeight: Int
) {
    constructor() : this(
        mapBottomPadding = 0,
        userSnippetYOffset = 0,
        eventMarkerYOffset = 0,
        eventsListsYOffset = 0,
        mapHeight = 0
    )
}
