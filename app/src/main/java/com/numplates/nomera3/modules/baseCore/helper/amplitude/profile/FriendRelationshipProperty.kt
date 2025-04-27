package com.numplates.nomera3.modules.baseCore.helper.amplitude.profile

import com.meera.application_api.analytic.model.AmplitudeProperty

enum class FriendRelationshipProperty(
    private val property: String
) : AmplitudeProperty {
    FRIEND("friend"),
    FOLLOWER("follower"),
    FOLLOW("follow"),
    MUTUAL_FOLLOW("mutual follow"),
    NOBODY("nobody"),
    NONE("none");

    override val _value: String
        get() = property

    override val _name: String
        get() = FriendRelationshipConst.RELATIONSHIP_EVENT_NAME
}

object FriendRelationshipConst {
    const val RELATIONSHIP_EVENT_NAME = "relationship"
}
