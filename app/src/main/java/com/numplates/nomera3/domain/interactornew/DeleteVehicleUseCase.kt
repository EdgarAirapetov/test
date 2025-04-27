package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class DeleteVehicleUseCase @Inject constructor(private val repository: ApiHiWayKt) {
    suspend fun invoke(vehicleId: String): ResponseWrapper<EmptyModel> {
        return repository.deleteVehicle(vehicleId = vehicleId)
    }
}
