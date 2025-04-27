package com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends

import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.HOW
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.OPEN_TYPE

enum class AmplitudeSelectedMutualFriendsTabProperty(
    private val property: String
) : AmplitudeProperty {
    FRIENDS("friends"),
    FOLLOWERS("followers"),
    FOLLOWS("follows"),
    MUTUAL_FOLLOWS("mutual follows");

    override val _value: String
        get() = property

    override val _name: String
        get() = OPEN_TYPE
}

enum class AmplitudeHowSelectedMutualFriendsProperty(
    private val property: String
) : AmplitudeProperty {

    USER_PROFILE("user profile"),
    TABS_INSIDE_BLOCK("tabs inside block");

    override val _value: String
        get() = property

    override val _name: String
        get() = HOW
}
