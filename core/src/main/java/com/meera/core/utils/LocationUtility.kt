package com.meera.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import timber.log.Timber

object LocationUtility {

    fun isLocationAvailable(act: Context?): Boolean {
        return isLocationEnabled(act) && checkPermissionLocation(act)
    }

    /**
     * LocationManagerCompat.isLocationEnabled() always returns true on some devices (e.g. Redmi 5),
     * so we have to also check Settings.Secure.LOCATION_PROVIDERS_ALLOWED, which will always
     * return null from Android S (12) (api 31) onwards
     */
    fun isLocationEnabled(context: Context?): Boolean {
        return (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { manager ->
            val locationEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val locationEnabledLegacy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val providersAllowed = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
                )
                !providersAllowed.isNullOrEmpty()
            } else {
                true
            }
            locationEnabled && locationEnabledLegacy
        } ?: false
    }

    fun checkPermissionLocation(act: Context?): Boolean {
        try {
            act?.let {
                return (ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            }
        }catch (e: Exception) {
            Timber.e(e)
            return false
        }
        return false
    }
}
