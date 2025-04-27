package com.numplates.nomera3.modules.maps.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.SetGPSRequestDto
import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.maps.data.mapper.MapDataMapper
import com.numplates.nomera3.modules.maps.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@AppScope
class LocationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val appSettings: AppSettings,
    private val apiHiWayKt: ApiHiWayKt,
    private val mapper: MapDataMapper
) : LocationRepository {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val lastLocationSharedFlow = MutableSharedFlow<Location>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, LOCATION_UPDATE_INTERVAL_MS)
        .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL_MS)
        .build()
    private val locationListener = LocationListener { location ->
        scope.launch {
            postLocationUpdate(location)
        }
    }
    private var isReceivingLocationUpdates = false

    init {
        lastLocationSharedFlow.distinctUntilChanged()
            .debounce(LOCATION_UPDATES_DEBOUNCE_MS)
            .onEach { location ->
                appSettings.writeLastLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                sendUserLocationToServer(
                    location = location,
                    retryCount = LOCATION_SEND_RETRY_COUNT
                )
            }
            .launchIn(scope)
    }

    override fun startReceivingLocationUpdates() {
        if (
            !isReceivingLocationUpdates
            && isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            && isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            isReceivingLocationUpdates = true
            updateWithLastLocation()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationListener, Looper.getMainLooper())
        }
    }

    override fun stopReceivingLocationUpdates() {
        updateWithLastLocation()
        fusedLocationClient.removeLocationUpdates(locationListener)
        isReceivingLocationUpdates = false
    }

    override fun locationFlow(): Flow<CoordinatesModel> = lastLocationSharedFlow
        .map(mapper::mapCoordinates)
        .distinctUntilChanged()

    override suspend fun getLastLocation(): CoordinatesModel? = getLastKnownLocation()
        ?.let(mapper::mapCoordinates)

    override fun readLastLocationFromStorage(): CoordinatesModel? = appSettings.readLastLocation()
        ?.let { (lat, lon) -> CoordinatesModel(lat = lat, lon = lon) }

    override suspend fun getCurrentLocation(): CoordinatesModel? = if (
        isLocationPermissionGranted()
    ) {
        getCurrentLocationHighAccuracy()?.let { currentLocation ->
            postLocationUpdate(currentLocation)
            mapper.mapCoordinates(currentLocation)
        }
    } else {
        null
    }

    private suspend fun getCurrentLocationHighAccuracy(): Location? {
        return  if (isGMSAvailable()) {
            fusedLocationClient.getCurrentLocation(getCurrentLocationRequest(), null).await()
        } else {
            null
        }
    }

    private fun isGMSAvailable(): Boolean {
        val gApi = GoogleApiAvailability.getInstance()
        val resultCode = gApi.isGooglePlayServicesAvailable(context)
        return resultCode ==
            com.google.android.gms.common.ConnectionResult.SUCCESS
    }

    private fun getCurrentLocationRequest() = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private fun updateWithLastLocation() {
        scope.launch {
            try {
                if (
                    isLocationPermissionGranted()
                ) {
                    getLastKnownLocation()?.let { location ->
                        postLocationUpdate(location)
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t)
            }
        }
    }

    private suspend fun getLastKnownLocation() : Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                IllegalArgumentException("Error while getting last known location $e")
            )
            null
        }
    }

    private suspend fun sendUserLocationToServer(location: Location, retryCount: Int) {
        if (retryCount <= 0) return
        try {
            apiHiWayKt.setGpsPosition(
                appSettings.readUID(),
                SetGPSRequestDto(gpsX = location.latitude, gpsY = location.longitude)
            )
        } catch (t: Throwable) {
            Timber.e(t)
            delay(LOCATION_SEND_RETRY_DELAY_MS)
            sendUserLocationToServer(
                location = location,
                retryCount = retryCount - 1
            )
        }
    }

    private fun isLocationPermissionGranted() = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        && isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun isPermissionGranted(permission: String): Boolean = ContextCompat
        .checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private suspend fun postLocationUpdate(location: Location) {
        val lastUpdateTime = lastLocationSharedFlow.replayCache.lastOrNull()?.time
        if (lastUpdateTime == null || lastUpdateTime < location.time) {
            lastLocationSharedFlow.emit(location)
        }
    }

    companion object {
        const val LOCATION_UPDATES_DEBOUNCE_MS = 500L
        const val LOCATION_SEND_RETRY_COUNT = 2
        const val LOCATION_SEND_RETRY_DELAY_MS = 3000L
        const val LOCATION_UPDATE_INTERVAL_MS = 30_000L
    }
}
