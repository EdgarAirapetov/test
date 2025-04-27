package com.numplates.nomera3.modules.registration.ui

import com.google.firebase.messaging.FirebaseMessaging
import com.meera.core.extensions.empty
import com.meera.core.extensions.getTimeZone
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.domain.interactornew.AuthAuthenticateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

private const val BEARER_NAME = "Bearer"
private const val ANDROID_DEVICE_NAME = "android"

class FirebasePushSubscriberDelegate @Inject constructor(
    private var authenticateUseCase: AuthAuthenticateUseCase,
    private var appSettings: AppSettings
) {

    fun subscribePush() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Fetching FCM registration token failed: ${task.exception}")
                return@addOnCompleteListener
            }

            val fcmToken = task.result

            Timber.e("FCM TOKEN $fcmToken")

            if (fcmToken != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    appSettings.writeFCMToken(fcmToken)
                    val locale = Locale.getDefault()
                    val timeZone = getTimeZone()

                    Timber.e("$BEARER_NAME FcmToken:$fcmToken")

                    appSettings.readAccessToken().let { accessToken ->
                        authenticateUseCase.subscribePush(
                            "$BEARER_NAME $accessToken",
                            fcmToken,
                            ANDROID_DEVICE_NAME,
                            locale.language,
                            timeZone
                        )
                    }
                }
            }
        }

    }

    fun subscribePush(fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            appSettings.writeFCMToken(fcmToken)
            val locale = Locale.getDefault()
            val timeZone = getTimeZone()

            Timber.e("GET FcmToken:$fcmToken")

            appSettings.readAccessToken().let { accessToken ->
                authenticateUseCase.subscribePush(
                    "$BEARER_NAME $accessToken",
                    fcmToken,
                    ANDROID_DEVICE_NAME,
                    locale.language,
                    timeZone
                )
            }
        }
    }

    // TODO: https://nomera.atlassian.net/browse/BR-20917
    suspend fun unsubscribePush(token: String): Boolean {
        val result = authenticateUseCase.unsubscribePush("$BEARER_NAME $token").isSuccess
        FirebaseMessaging.getInstance().deleteToken().await()
        appSettings.writeFCMToken(String.empty())

        return result
    }

    suspend fun unsubscribePush(): Boolean {
        val token = appSettings.readAccessToken()
        val result = authenticateUseCase.unsubscribePush("$BEARER_NAME $token").isSuccess
        FirebaseMessaging.getInstance().deleteToken().await()
        appSettings.writeFCMToken(String.empty())

        return result
    }

}
