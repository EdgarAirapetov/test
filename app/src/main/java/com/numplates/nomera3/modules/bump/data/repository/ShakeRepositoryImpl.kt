package com.numplates.nomera3.modules.bump.data.repository

import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.datastore.Preference
import com.numplates.nomera3.data.network.UpdateFriendshipDtoModel
import com.numplates.nomera3.domain.model.UpdateFriendshipModel
import com.numplates.nomera3.domain.model.WebSocketConnectionState
import com.numplates.nomera3.modules.bump.data.api.ShakeApi
import com.numplates.nomera3.modules.bump.data.entity.ShakeDataEvent
import com.numplates.nomera3.modules.bump.data.entity.UserShakeListDtoModel
import com.numplates.nomera3.modules.bump.data.mapper.ShakeDataMapper
import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_SHAKE_ENABLED_STATE = false

@AppScope
class ShakeRepositoryImpl @Inject constructor(
    private val shakeApi: ShakeApi,
    private val webSocketMainChannel: WebSocketMainChannel,
    private val gson: Gson,
    private val dataMapper: ShakeDataMapper,
    private val appSettings: AppSettings
) : ShakeRepository {

    private val registerShakeEventFlow = MutableSharedFlow<ShakeDataEvent>()
    private val shakeUsersResultState = MutableStateFlow<List<UserShakeModel>?>(null)

    override suspend fun writeNeedToRegisterShakeEventListener(isNeed: Boolean) {
        appSettings.shakeEnabledPrivacy.set(isNeed)
    }

    override fun readNeedToRegisterShakeEventListener(): Boolean = appSettings.shakeEnabledPrivacy.getSync()
        ?: DEFAULT_SHAKE_ENABLED_STATE

    override fun observeShakeRegisteredChanged(): SharedFlow<ShakeDataEvent> =
        registerShakeEventFlow.asSharedFlow()

    override suspend fun tryToRegisterShakeEvent(isNeedToRegister: Boolean) {
        registerShakeEventFlow.emit(ShakeDataEvent.TryToRegisterShakeEvent(isNeedToRegister))
    }

    override suspend fun forceToRegisterShakeEvent(isNeedToRegister: Boolean) {
        registerShakeEventFlow.emit(ShakeDataEvent.ForceToRegisterShakeEvent(isNeedToRegister))
    }

    override suspend fun emitShakeUsersNotFound() {
        registerShakeEventFlow.emit(ShakeDataEvent.ShakeUserNotFoundEvent)
    }

    override suspend fun setShakeCoordinates(gpsX: Float, gpsY: Float): Any {
        return shakeApi.setShakeCoordinates(
            gpsX = gpsX,
            gpsY = gpsY
        )
    }

    override suspend fun deleteShakeUser() {
        shakeApi.deleteShakeUser()
    }

    override fun observeShakeFriendRequests(): Flow<List<UserShakeModel>> {
        return webSocketMainChannel.observeShakeFriendRequests()
            .map { message ->
                Timber.d("ShakeFriendRequests: $message")
                val dataEntity = gson.fromJson<UserShakeListDtoModel>(gson.toJson(message.payload))
                val result = dataEntity.userShakeList?.let { dataMapper.map(it) } ?: listOf()
                shakeUsersResultState.value = result
                result
            }
    }

    override fun getShakeUsersResult(): Flow<List<UserShakeModel>?> {
        return shakeUsersResultState.asSharedFlow()
    }

    override fun clearShakeUsers() {
        shakeUsersResultState.value = null
    }

    override fun observeUpdateFriendship(): Flow<UpdateFriendshipModel> {
        return webSocketMainChannel.observeUpdateFriendship()
            .flowOn(Dispatchers.IO)
            .map { message ->
                gson.fromJson<UpdateFriendshipDtoModel>(gson.toJson(message.payload))
            }
            .map(dataMapper::mapUpdateFriendship)
    }

    override fun hasActiveShakeUsers(): Boolean {
        return !shakeUsersResultState.value.isNullOrEmpty()
    }

    override suspend fun observeShakePrivacySettingChanged(): Preference<Boolean> {
        return appSettings.shakeEnabledPrivacy
    }

    override fun observeWebSocketConnection(): Flow<WebSocketConnectionState> {
        return callbackFlow {
            webSocketMainChannel.addWebSocketConnectionListener(object : WebSocketMainChannel.WebSocketConnectionListener {
                override fun connectionStatus(isConnected: Boolean) {
                    val connectionStatus = if (isConnected) WebSocketConnectionState.CONNECTED else
                        WebSocketConnectionState.DISCONNECTED
                    trySend(connectionStatus)
                }
            })
            awaitClose()
        }
    }
}
