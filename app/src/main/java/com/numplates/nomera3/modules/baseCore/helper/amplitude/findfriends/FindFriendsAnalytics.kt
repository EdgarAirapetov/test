package com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeFindFriends {
    fun onFindFriendsPressed(pressedWhere: AmplitudeFindFriendsWhereProperty)
}

class AmplitudeFindFriendsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
): AmplitudeFindFriends {

    override fun onFindFriendsPressed(pressedWhere: AmplitudeFindFriendsWhereProperty) {
        delegate.logEvent(
            eventName = AmplitudeFindFriendsEventName.FRIENDS_FIND,
            properties = {
                it.apply {
                    addProperty(pressedWhere)
                }
            }
        )
    }
}
