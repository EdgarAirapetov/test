package com.numplates.nomera3.modules.maps.ui.events.navigation.mapper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGeoServiceName
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationUiModel
import javax.inject.Inject

class EventNavigationUiMapper @Inject constructor(private val context: Context) {

    fun mapUiModel(eventUiModel: EventUiModel): EventNavigationUiModel {
        val location = eventUiModel.address.location
        val items = listOf(
            getGoogleNavigationUri(location),
            getYandexMapsNavigationUri(location),
            getYandexNavigatorNavigationUri(location),
            getWazeNavigationUri(location),
            get2GisNavigationUri(location),
            getSygicNavigationUri(location)
        )
            .filter { isNavigatorAvailable(appName = it.appName, navigatorIntent = it.navigatorIntent) }
        val address = "${eventUiModel.address.name}, ${eventUiModel.address.addressString}"
        return EventNavigationUiModel(
            items = items,
            address = address
        )
    }

    fun mapAmplitudePropertyMapEventsGeoServiceName(appName: String): AmplitudePropertyMapEventsGeoServiceName =
        when (appName) {
            APP_NAME_GOOGLE_MAPS -> AmplitudePropertyMapEventsGeoServiceName.GOOGLE
            APP_NAME_YANDEX_MAPS -> AmplitudePropertyMapEventsGeoServiceName.YANDEX
            APP_NAME_YANDEX_NAVIGATOR -> AmplitudePropertyMapEventsGeoServiceName.YANDEX_NAVIGATOR
            APP_NAME_WAZE -> AmplitudePropertyMapEventsGeoServiceName.WAZE
            APP_NAME_2GIS -> AmplitudePropertyMapEventsGeoServiceName.TWOGIS
            APP_NAME_SYGIC -> AmplitudePropertyMapEventsGeoServiceName.SYGIC_GPS
            else -> AmplitudePropertyMapEventsGeoServiceName.OTHER
        }

    private fun isNavigatorAvailable(appName: String, navigatorIntent: Intent): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isNavigatorAvailableTiramisuPlus(appName = appName, navigatorIntent = navigatorIntent)
        } else {
            isNavigatorAvailableLegacy(appName = appName, navigatorIntent = navigatorIntent)
        }

    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNavigatorAvailableTiramisuPlus(appName: String, navigatorIntent: Intent): Boolean =
        if (appName == APP_NAME_WAZE) {
            context.packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                .any { it.packageName == PACKAGE_NAME_WAZE }
        } else {
            context.packageManager.queryIntentActivities(
                navigatorIntent,
                PackageManager.ResolveInfoFlags.of(0)
            ).isNotEmpty()
        }

    @SuppressLint("QueryPermissionsNeeded")
    private fun isNavigatorAvailableLegacy(appName: String, navigatorIntent: Intent): Boolean =
        if (appName == APP_NAME_WAZE) {
            context.packageManager.getInstalledPackages(0)
                .any { it.packageName == PACKAGE_NAME_WAZE }
        } else {
            context.packageManager.queryIntentActivities(navigatorIntent, 0).isNotEmpty()
        }

    private fun mapNavigatorIntent(navigatorUri: String, navigatorPackage: String?) =
        Intent(Intent.ACTION_VIEW, Uri.parse(navigatorUri)).apply {
            navigatorPackage?.let(::setPackage)
        }

    private fun getGoogleNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "google.navigation:q=${location.latitude},${location.longitude}",
            navigatorPackage = "com.google.android.apps.maps"
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_GOOGLE_MAPS,
            iconResId = R.drawable.ic_navigation_google_maps,
            titleResId = R.string.map_events_navigation_google_maps,
            navigatorIntent = intent
        )
    }

    private fun getYandexMapsNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "yandexmaps://maps.yandex.ru/?rtext=~${location.latitude},${location.longitude}&rtt=auto",
            navigatorPackage = null
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_YANDEX_MAPS,
            iconResId = R.drawable.ic_navigation_yandex_maps,
            titleResId = R.string.map_events_navigation_yandex_maps,
            navigatorIntent = intent
        )
    }

    private fun getYandexNavigatorNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "yandexnavi://build_route_on_map?lat_to=${location.latitude}&lon_to=${location.longitude}",
            navigatorPackage = "ru.yandex.yandexnavi"
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_YANDEX_NAVIGATOR,
            iconResId = R.drawable.ic_navigation_yandex_navigator,
            titleResId = R.string.map_events_navigation_yandex_navigator,
            navigatorIntent = intent
        )
    }

    private fun getWazeNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "https://www.waze.com/ul?ll=${location.latitude}%2C${location.longitude}&navigate=yes",
            navigatorPackage = null
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_WAZE,
            iconResId = R.drawable.ic_navigation_waze,
            titleResId = R.string.map_events_navigation_waze,
            navigatorIntent = intent
        )
    }

    private fun get2GisNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "dgis://2gis.ru/routeSearch/rsType/car/to/${location.longitude},${location.latitude}",
            navigatorPackage = "ru.dublgis.dgismobile"
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_2GIS,
            iconResId = R.drawable.ic_navigation_2gis,
            titleResId = R.string.map_events_navigation_2gis,
            navigatorIntent = intent
        )
    }

    private fun getSygicNavigationUri(location: LatLng): EventNavigationItemUiModel {
        val intent = mapNavigatorIntent(
            navigatorUri = "com.sygic.aura://coordinate|${location.longitude}|${location.latitude}|drive",
            navigatorPackage = null
        )
        return EventNavigationItemUiModel(
            appName = APP_NAME_SYGIC,
            iconResId = R.drawable.ic_navigation_sygic,
            titleResId = R.string.map_events_navigation_sygic,
            navigatorIntent = intent
        )
    }

    companion object {
        private const val APP_NAME_GOOGLE_MAPS ="GoogleMaps"
        private const val APP_NAME_YANDEX_MAPS ="YandexMaps"
        private const val APP_NAME_YANDEX_NAVIGATOR ="YandexNavigator"
        private const val APP_NAME_WAZE ="Waze"
        private const val APP_NAME_2GIS ="2Gis"
        private const val APP_NAME_SYGIC ="Sygic"

        private const val PACKAGE_NAME_WAZE ="com.waze"
    }
}
