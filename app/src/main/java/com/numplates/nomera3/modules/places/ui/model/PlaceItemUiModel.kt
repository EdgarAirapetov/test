package com.numplates.nomera3.modules.places.ui.model

import com.numplates.nomera3.modules.places.domain.model.PlaceModel

data class PlaceItemUiModel(
    val title: String,
    val address: String,
    val place: PlaceModel
)
