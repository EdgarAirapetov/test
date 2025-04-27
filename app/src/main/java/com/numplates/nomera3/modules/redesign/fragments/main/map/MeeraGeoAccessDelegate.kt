package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.Manifest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.LocationUtility
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import timber.log.Timber

/**
 * We need to receive permission first and location services access second. If user does not grant permission
 * during first step – stop the process
 */
class MeeraGeoAccessDelegate(
    private val act: MeeraAct,
    permissionDelegate: PermissionDelegate?,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver {
    private val geoAccessActions = mutableListOf<() -> Unit>()
    private var userNavigatesToAppSettings = false
    private var deniedAndNoRationaleNeededBeforeRequest = false
    private val permissionListener = object : PermissionDelegate.Listener {
        override fun onGranted() {
            Timber.d("Location permission granted")
        }

        override fun onDenied() {
            Timber.d("Location permission denied")
            val deniedAndNoRationaleNeededAfterRequest = !act.shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (deniedAndNoRationaleNeededBeforeRequest && deniedAndNoRationaleNeededAfterRequest) {
                act.sendUserToAppSettings()
                userNavigatesToAppSettings = true
            }
        }

        override fun onError(error: Throwable?) {
            Timber.e(error)
        }
    }
    private val locationDelegate = MeeraLocationDelegate(
        act = act,
        permissionDelegate = permissionDelegate,
        permissionListener = permissionListener
    )

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (userNavigatesToAppSettings) {
            userNavigatesToAppSettings = false
        } else {
            geoAccessActions.removeFirstOrNull()?.invoke()
        }
    }

    fun isGeoAccessProvided(): Boolean = LocationUtility.isLocationAvailable(act)

    fun provideGeoAccess() {
        if (geoAccessActions.isNotEmpty()) return
        if (!locationDelegate.isPermissionGranted()) {
            geoAccessActions.add {
                deniedAndNoRationaleNeededBeforeRequest = !act.shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                locationDelegate.requestLocationPermissions()
            }
        }
        if (!locationDelegate.isLocationEnabled()) {
            geoAccessActions.add {
                if (locationDelegate.isPermissionGranted()) {
                    locationDelegate.requestEnableLocation()
                }
            }
        }
        geoAccessActions.removeFirstOrNull()?.invoke()
    }
}
