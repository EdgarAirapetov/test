package com.numplates.nomera3.modules.vehicle

import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class VehicleUpdateSubjectUseCase @Inject constructor(
    val repository: VehicleRepository
) : BaseUseCaseNoSuspend<VehicleUpdateSubjectParams, PublishSubject<Vehicle?>> {

    override fun execute(params: VehicleUpdateSubjectParams): PublishSubject<Vehicle?> {
        return repository.getVehicleUpdateSubject()
    }

}

object VehicleUpdateSubjectParams: DefParams()