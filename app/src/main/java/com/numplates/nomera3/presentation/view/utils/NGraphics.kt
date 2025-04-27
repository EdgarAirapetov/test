package com.numplates.nomera3.presentation.view.utils

import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.VehicleTypeEntity

object NGraphics {
    /**
     * Не выносил в CORE модуль т.к. жёстко привязано к фуекционалу фичи
     */
    @JvmStatic
    fun getVehicleTypeMap(): MutableMap<String, VehicleTypeEntity> {
        val vehicleTypeMap: MutableMap<String, VehicleTypeEntity> = HashMap()
        vehicleTypeMap["1"] =
            VehicleTypeEntity(R.drawable.vehicle_car, R.drawable.vehicle_car_selected, R.drawable.vehicle_car_selected)
        vehicleTypeMap["2"] = VehicleTypeEntity(
            R.drawable.vehicle_motorbike,
            R.drawable.vehicle_motorbike_selected,
            R.drawable.vehicle_motorbike_selected
        )
        vehicleTypeMap["3"] = VehicleTypeEntity(
            R.drawable.vehicle_bycicle,
            R.drawable.vehicle_bycicle_selected,
            R.drawable.vehicle_bycicle_selected
        )
        vehicleTypeMap["4"] = VehicleTypeEntity(
            R.drawable.vehicle_kickscooter,
            R.drawable.vehicle_kickscooter_selected,
            R.drawable.vehicle_kickscooter_selected
        )
        vehicleTypeMap["5"] = VehicleTypeEntity(
            R.drawable.vehicle_skateboard,
            R.drawable.vehicle_skateboard_selected,
            R.drawable.vehicle_skateboard_selected
        )
        vehicleTypeMap["6"] = VehicleTypeEntity(
            R.drawable.vehicle_rollerskates,
            R.drawable.vehicle_rollerskates_selected,
            R.drawable.vehicle_rollerskates_selected
        )
        vehicleTypeMap["7"] = VehicleTypeEntity(
            R.drawable.vehicle_snowmobile,
            R.drawable.vehicle_snowmobile_selected,
            R.drawable.vehicle_snowmobile_selected
        )
        vehicleTypeMap["8"] = VehicleTypeEntity(
            R.drawable.vehicle_jetski,
            R.drawable.vehicle_jetski_selected,
            R.drawable.vehicle_jetski_selected
        )
        vehicleTypeMap["9"] = VehicleTypeEntity(
            R.drawable.vehicle_plane,
            R.drawable.vehicle_plane_selected,
            R.drawable.vehicle_plane_selected
        )
        vehicleTypeMap["10"] = VehicleTypeEntity(
            R.drawable.vehicle_boat,
            R.drawable.vehicle_boat_selected,
            R.drawable.vehicle_boat_selected
        )
        return vehicleTypeMap
    }

}
