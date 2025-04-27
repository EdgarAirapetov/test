package com.numplates.nomera3.modules.maps.domain.model

enum class FilterVehicleType(val value: Int) {
    CARS(1),
    BIKES(2),
    BICYCLES(3);

    companion object {

        @JvmStatic
        fun allValue(): String {
            return values()
                .map { it.value }
                .joinToString(",")
        }
    }
}