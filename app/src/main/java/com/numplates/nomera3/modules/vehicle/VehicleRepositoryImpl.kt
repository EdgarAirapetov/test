package com.numplates.nomera3.modules.vehicle

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.network.Vehicle
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AppScope
class VehicleRepositoryImpl @Inject constructor(): VehicleRepository {

    var onVehicleUpdateSubject = PublishSubject.create<Vehicle?>()

    override fun getVehicleUpdateSubject(): PublishSubject<Vehicle?> {
        return onVehicleUpdateSubject
    }
}
