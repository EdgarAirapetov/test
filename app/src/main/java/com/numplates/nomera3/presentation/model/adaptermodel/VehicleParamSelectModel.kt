package com.numplates.nomera3.presentation.model.adaptermodel

import com.numplates.nomera3.data.network.market.Value

data class VehicleParamSelectModel(
        var value: Value,
        var isChecked: Boolean = false
)