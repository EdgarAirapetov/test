package com.numplates.nomera3.modules.maps.data

import android.Manifest
import android.app.Activity
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices

class LastLocationProvider(activity: Activity) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    var listener: ((Location?) -> Unit)? = null

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            listener?.invoke(location)
        }
    }
}
