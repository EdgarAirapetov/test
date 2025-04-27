package com.meera.analytics.referrals

import com.meera.analytics.amplitude.AmplitudeEventDelegate
import com.meera.analytics.amplitude.AmplitudePropertyNameConst
import com.meera.analytics.amplitude.addProperty
import com.meera.analytics.referrals.AmplitudeReferralPropertyNameConst.EXPIRATION_DATE
import javax.inject.Inject

interface ReferralsAnalytics {

    fun logBuyVipStatus(
        color: AmplitudePropertyColor,
        duration: AmplitudePropertyDuration,
        expirationDate: String,
        haveVipBefore: AmplitudePropertyHaveVIPBefore,
        way: AmplitudePropertyWay
    )

    fun onSendInvitation(userId: Long)

    fun onCodeCopied(userId: Long)
}

class ReferralAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : ReferralsAnalytics {

    override fun logBuyVipStatus(
        color: AmplitudePropertyColor,
        duration: AmplitudePropertyDuration,
        expirationDate: String,
        haveVipBefore: AmplitudePropertyHaveVIPBefore,
        way: AmplitudePropertyWay
    ) {
        delegate.logEvent(
            eventName = AmplitudeReferralEventName.VIP_BUYING,
            properties = {
                it.apply {
                    addProperty(color)
                    addProperty(duration)
                    addProperty(haveVipBefore)
                    addProperty(EXPIRATION_DATE, expirationDate)
                    addProperty(way)
                }
            }
        )
    }

    override fun onSendInvitation(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeReferralSendInvitationEvent.SEND_INVITATION_TAP,
            properties = { json ->
                json.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(AmplitudeReferralSendInvitationType.BUTTON)
                }
            }
        )
    }

    override fun onCodeCopied(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeReferralSendInvitationEvent.SEND_INVITATION_TAP,
            properties = { json ->
                json.apply {
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(AmplitudeReferralSendInvitationType.CODE)
                }
            }
        )
    }
}
