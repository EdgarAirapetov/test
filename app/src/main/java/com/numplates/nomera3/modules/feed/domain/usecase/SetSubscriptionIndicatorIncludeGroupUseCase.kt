package com.numplates.nomera3.modules.feed.domain.usecase


import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.data.network.PrivacySetting
import org.phoenixframework.Message
import timber.log.Timber
import javax.inject.Inject

private const val SUBSCRIPTION_INCLUDE_GROUP_SETTINGS = "subscriptionsIncludeGroups"
private const val SETTINGS_KEY = "settings"

class SetSubscriptionIndicatorIncludeGroupUseCase @Inject constructor(private val socket: WebSocketMainChannel) {
    suspend fun execute(isIncludeGroups: Boolean): Message? {
        return try {
            val isIncludeGroupInteger = if (isIncludeGroups) {
                1
            } else {
                0
            }

            val settings = mutableListOf(PrivacySetting(SUBSCRIPTION_INCLUDE_GROUP_SETTINGS, isIncludeGroupInteger))
            val payload = mutableMapOf<String, Any>(
                SETTINGS_KEY to settings
            )

            socket.pushSetPrivacySettingsSuspended(payload)
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            null
        }
    }
}
