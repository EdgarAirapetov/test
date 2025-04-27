package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class FriendRequestEvent(
    private val event: String
) : AmplitudeName {
    FRIEND_ANSWER("friend add answer");

    override val eventName: String
        get() = event
}

enum class FriendRequestType(
    private val property: String
) : AmplitudeProperty {

    ACCEPT("accept"),
    REJECT("reject");

    override val _value: String
        get() = property

    override val _name: String
        get() = FriendRequestConst.REQUEST_ACTION_TYPE
}

object FriendRequestConst {
    const val REQUEST_ACTION_TYPE = "action type"
}


