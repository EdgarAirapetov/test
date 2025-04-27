package com.numplates.nomera3.modules.baseCore.helper.amplitude.sendinvitation

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeSendInvitationAnalytics {
    fun onSendInvitation()
    fun onCodeCopied()
}

@Deprecated("Not used, should delete")
class AmplitudeSendInvitationAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeSendInvitationAnalytics {

    @Deprecated("Not used, should delete")
    override fun onSendInvitation() {
        delegate.logEvent(
            eventName = AmplitudeSendInvitationEvent.SEND_INVITATION_TAP,
            properties = { json ->
                json.apply {
                    addProperty(AmplitudeSendInvitationType.BUTTON)
                }
            }
        )
    }

    @Deprecated("Not used, should delete")
    override fun onCodeCopied() {
        delegate.logEvent(
            eventName = AmplitudeSendInvitationEvent.SEND_INVITATION_TAP,
            properties = { json ->
                json.apply {
                    addProperty(AmplitudeSendInvitationType.CODE)
                }
            }
        )
    }
}
