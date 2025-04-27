package com.numplates.nomera3.presentation.model.adaptermodel

import com.numplates.nomera3.data.network.Vehicle

data class VehicleModel(
    val vehicle: Vehicle?,
    val hidden: Boolean = false
)
