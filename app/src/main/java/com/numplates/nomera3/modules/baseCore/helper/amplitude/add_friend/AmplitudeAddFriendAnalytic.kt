package com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import javax.inject.Inject

interface AmplitudeAddFriendAnalytic {

    fun logAddFriend(
        fromId: Long,
        toId: Long,
        type: FriendAddAction = FriendAddAction.OTHER,
        influencer: AmplitudeInfluencerProperty
    )
}

class AmplitudeAddFriendAnalyticImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeAddFriendAnalytic{

    override fun logAddFriend(
        fromId: Long,
        toId: Long,
        type: FriendAddAction,
        influencer: AmplitudeInfluencerProperty
    ) {
        delegate.logEvent(
            eventName = AmplitudeAddFriendEventName.FRIEND_ADD,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, fromId)
                    addProperty(AmplitudePropertyNameConst.TO, toId)
                    addProperty(type)
                    addProperty(influencer)
                }
            }
        )
    }

}
