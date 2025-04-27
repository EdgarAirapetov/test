package com.numplates.nomera3.modules.bump.domain.repository

import com.meera.core.preferences.datastore.Preference
import com.numplates.nomera3.domain.model.UpdateFriendshipModel
import com.numplates.nomera3.domain.model.WebSocketConnectionState
import com.numplates.nomera3.modules.bump.data.entity.ShakeDataEvent
import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface ShakeRepository {

    suspend fun writeNeedToRegisterShakeEventListener(isNeed: Boolean)

    fun readNeedToRegisterShakeEventListener(): Boolean

    fun observeShakeRegisteredChanged(): SharedFlow<ShakeDataEvent>

    fun observeShakeFriendRequests() : Flow<List<UserShakeModel>>

    suspend fun tryToRegisterShakeEvent(isNeedToRegister: Boolean)

    suspend fun forceToRegisterShakeEvent(isNeedToRegister: Boolean)

    suspend fun setShakeCoordinates(gpsX: Float, gpsY: Float): Any

    suspend fun deleteShakeUser()

    suspend fun emitShakeUsersNotFound()

    fun observeUpdateFriendship(): Flow<UpdateFriendshipModel>

    fun observeWebSocketConnection() : Flow<WebSocketConnectionState>

    fun getShakeUsersResult() : Flow<List<UserShakeModel>?>

    suspend fun observeShakePrivacySettingChanged(): Preference<Boolean>

    fun hasActiveShakeUsers(): Boolean

    fun clearShakeUsers()
}
