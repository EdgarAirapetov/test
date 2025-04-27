package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

/**
 * Отмечаем, когда пользователь нажал на кнопку "Пригласить друзей"
 */
interface FriendInviteTapAnalytics {
    fun logFiendInviteTap(where: FriendInviteTapProperty)
}

class FriendInviteTapAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : FriendInviteTapAnalytics {

    override fun logFiendInviteTap(where: FriendInviteTapProperty) {
        delegate.logEvent(
            eventName = FriendInviteTapEventName.FRIEND_INVITE_TAP,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }
}
