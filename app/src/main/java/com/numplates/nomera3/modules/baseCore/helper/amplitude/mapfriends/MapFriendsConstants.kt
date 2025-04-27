package com.numplates.nomera3.modules.baseCore.helper.amplitude.mapfriends

import com.meera.application_api.analytic.model.AmplitudeName

enum class AmplitudeMapFriendsEventName(
    private val event: String
) : AmplitudeName {
    MAP_FRIENDS_LIST_PRESS("friends list press");

    override val eventName: String
        get() = event
}
