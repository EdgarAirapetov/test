package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.FROM
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.TO
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeFriendRequest {
    fun onRequestAccepted(
        fromId: Long,
        toId: Long,
        where: AmplitudeFriendRequestPropertyWhere = AmplitudeFriendRequestPropertyWhere.OTHER
    )
    fun onRequestDenied(
        fromId: Long,
        toId: Long,
        where: AmplitudeFriendRequestPropertyWhere = AmplitudeFriendRequestPropertyWhere.OTHER
    )
}

class AmplitudeFriendRequestImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeFriendRequest {

    override fun onRequestAccepted(
        fromId: Long,
        toId: Long,
        where: AmplitudeFriendRequestPropertyWhere
    ) {
        delegate.logEvent(
            eventName = FriendRequestEvent.FRIEND_ANSWER,
            properties = { json ->
                json.apply {
                    addProperty(FriendRequestType.ACCEPT)
                    addProperty(FROM, fromId)
                    addProperty(TO, toId)
                    addProperty(where)
                }
            }
        )
    }

    override fun onRequestDenied(
        fromId: Long,
        toId: Long,
        where: AmplitudeFriendRequestPropertyWhere
    ) {
        delegate.logEvent(
            eventName = FriendRequestEvent.FRIEND_ANSWER,
            properties = { json ->
                json.apply {
                    addProperty(FriendRequestType.REJECT)
                    addProperty(FROM, fromId)
                    addProperty(TO, toId)
                    addProperty(where)
                }
            }
        )
    }
}
