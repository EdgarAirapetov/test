package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun startReceivingLocationUpdates()
    fun stopReceivingLocationUpdates()
    fun locationFlow(): Flow<CoordinatesModel>
    suspend fun getLastLocation(): CoordinatesModel?
    fun readLastLocationFromStorage(): CoordinatesModel?
    suspend fun getCurrentLocation(): CoordinatesModel?
}
