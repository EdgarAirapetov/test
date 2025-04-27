package com.numplates.nomera3.modules.vehicle

import com.numplates.nomera3.data.network.Vehicle
import io.reactivex.subjects.PublishSubject


interface VehicleRepository {
    fun getVehicleUpdateSubject(): PublishSubject<Vehicle?>
}