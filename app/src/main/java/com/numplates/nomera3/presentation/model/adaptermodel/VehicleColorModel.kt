package com.numplates.nomera3.presentation.model.adaptermodel

import com.numplates.nomera3.data.network.market.Value

class VehicleColorModel(var color: Int, var colorName: String, var value: Value) {
    var isSelected: Boolean = false
}