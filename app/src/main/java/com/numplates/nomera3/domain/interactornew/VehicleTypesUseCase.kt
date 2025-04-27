package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay

class VehicleTypesUseCase(private val repository: ApiHiWay) {

    fun getVehicleTypes() = repository.vehicleTypes

}