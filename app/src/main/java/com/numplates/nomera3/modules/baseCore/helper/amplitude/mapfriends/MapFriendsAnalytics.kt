package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapfriends

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import javax.inject.Inject

interface AmplitudeMapFriends {
    fun onFriendsListPress(
        userId: Long
    )
}

class AmplitudeMapFriendsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMapFriends {

    override fun onFriendsListPress(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMapFriendsEventName.MAP_FRIENDS_LIST_PRESS,
            properties = {
                it.apply {
                    addProperty(
                        propertyName = AmplitudePropertyNameConst.USER_ID,
                        value = userId
                    )
                }
            }
        )
    }
}
