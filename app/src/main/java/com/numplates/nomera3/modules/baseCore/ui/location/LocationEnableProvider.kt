package com.numplates.nomera3.modules.baseCore.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import timber.log.Timber
import javax.inject.Inject

class LocationEnableProvider @Inject constructor(
    private val context: Context
) {
    fun hasLocationPermission(): Boolean = try {
        (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    fun isCoarseLocation(): Boolean = try {
        (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
    } catch (e: Exception) {
        Timber.e(e)
        false
    }

    fun isLocationEnabled(): Boolean {
        return try {
            (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { manager ->
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } ?: false
        } catch (e: Exception) {
            Timber.e(e)
            false
        }
    }
}
