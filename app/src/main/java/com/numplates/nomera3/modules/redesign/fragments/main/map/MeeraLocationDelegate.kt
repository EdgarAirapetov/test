package com.numplates.nomera3.modules.redesign.fragments.main.map

import android.Manifest
import android.content.Intent
import android.provider.Settings
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.LocationUtility
import com.numplates.nomera3.modules.baseCore.ui.location.LocationContract
import com.numplates.nomera3.modules.redesign.MeeraAct


open class MeeraLocationDelegate(
    private val act: MeeraAct?,
    private val permissionDelegate: PermissionDelegate?,
    private val permissionListener: PermissionDelegate.Listener?
) : LocationContract {

    override fun isLocationEnabled(): Boolean = LocationUtility.isLocationEnabled(act)

    override fun isPermissionGranted(): Boolean = LocationUtility.checkPermissionLocation(act)

    override fun requestLocationPermissions() {
        permissionDelegate?.setPermissions(
            permissionListener,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun requestEnableLocation() {
        act?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
}
