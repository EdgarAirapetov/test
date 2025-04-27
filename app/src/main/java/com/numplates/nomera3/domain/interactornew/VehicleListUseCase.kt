package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable

class VehicleListUseCase(private val repository: ApiHiWay) {
    fun getVehicleListUseCase(userID: Long): Flowable<ResponseWrapper<Vehicles>> = repository.vehicleList(userID)
}