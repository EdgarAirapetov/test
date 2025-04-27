package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWayKt
import timber.log.Timber
import javax.inject.Inject

class AuthAuthenticateUseCase @Inject constructor(
    private val apiHiWay: ApiHiWayKt
) {

    suspend fun subscribePush(
        token: String,
        deviceToken: String,
        device: String,
        locale: String,
        timeZone: Float
    ) =
        runCatching {
            apiHiWay.subscribePushNotifications(token, deviceToken, device, locale, timeZone)
        }.onFailure {
            Timber.e("AuthAuthenticateUseCase pushSubscribe ERROR  $it")
            it.printStackTrace()
        }.onSuccess {
            Timber.d("AuthAuthenticateUseCase pushSubscribe SUCCESS $it")
        }

    suspend fun unsubscribePush(token: String) =
        runCatching {
            apiHiWay.unsubscribePushNotifications(token)
        }.onFailure {
            Timber.e("AuthAuthenticateUseCase unsubscribePush ERROR $it")
            it.printStackTrace()
        }.onSuccess {
            Timber.d("AuthAuthenticateUseCase unsubscribePush SUCCESS $it")
        }

}
