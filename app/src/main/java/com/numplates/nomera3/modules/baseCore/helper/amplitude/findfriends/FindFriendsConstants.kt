package com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeFindFriendsEventName(private val event: String) : AmplitudeName {

    FRIENDS_FIND("friends find");

    override val eventName: String
        get() = event
}

enum class AmplitudeFindFriendsWhereProperty(val property: String) : AmplitudeProperty {
    FRIENDS("friends"),
    SHARE("share"),
    MESSAGE("message");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudeFindFriendsPropertyName.WHERE
}

object AmplitudeFindFriendsPropertyName {
    const val WHERE = "where"
}
