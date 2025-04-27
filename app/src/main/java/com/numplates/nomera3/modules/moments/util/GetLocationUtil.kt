package com.numplates.nomera3.modules.moments.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.annotation.RequiresPermission
import com.numplates.nomera3.modules.maps.data.LastLocationProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

private const val GET_LOCATION_TIME_OUT_MS = 5000L

class GetLocationUtil(activity: Activity) {

    private var lastLocationProvider = activity.let(::LastLocationProvider)
    private var rxPermissions = activity.let(::RxPermissions)

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): SimpleLocation {
        return runCatching {
            val isGranted = getGrantedLocationPermission()
            if (isGranted) {
                withTimeout(GET_LOCATION_TIME_OUT_MS) {
                    val rawLocation = getRawLocation()
                    SimpleLocation(rawLocation.latitude, rawLocation.altitude)
                }
            } else {
                error("Location Permission is not granted")
            }
        }.getOrDefault(getUndefinedSimpleLocation())
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun getRawLocation() = suspendCancellableCoroutine { continuation ->
        lastLocationProvider.listener = { location ->
            lastLocationProvider.listener = null

            if (location != null) {
                continuation.resume(location)
            } else {
                continuation.cancel(IllegalStateException("Location can't be null"))
            }
        }
        lastLocationProvider.getLastKnownLocation()
    }

    private suspend fun getGrantedLocationPermission() = suspendCancellableCoroutine { continuation ->
        val isGranted = rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            && rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        continuation.resume(isGranted)
    }

    private fun getUndefinedSimpleLocation() = SimpleLocation(-1.0, -1.0)

    data class SimpleLocation(val x: Double, val y: Double)
}
