package com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeAddFriendEventName(
    private val event: String
) : AmplitudeName {

    FRIEND_ADD("friend add");

    override val eventName: String
        get() = event
}

enum class AmplitudeAddFriendInfluencerProperty(
    private val property: String
) : AmplitudeProperty {
    TRUE("true"),
    FALSE("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.INFLUENCER

    override val _value: String
        get() = property
}
