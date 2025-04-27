package com.numplates.nomera3.modules.baseCore.ui.location

interface LocationContract {
    fun isLocationEnabled(): Boolean
    fun isPermissionGranted(): Boolean
    fun requestLocationPermissions()
    fun requestEnableLocation()
}
