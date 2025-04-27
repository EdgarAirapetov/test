package com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import javax.inject.Inject

interface AmplitudeFollowButton {

    /**
     * Пользователь подписался на другого пользователя
     */
    fun followAction(
        fromId: Long,
        toId: Long,
        where: AmplitudeFollowButtonPropertyWhere,
        type: AmplitudePropertyType,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty
    )

    /**
     * Пользователь отписался от другого пользователя
     */
    fun logUnfollowAction(
        fromId: Long,
        toId: Long,
        where: AmplitudeFollowButtonPropertyWhere,
        type: AmplitudePropertyType,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty
    )
}

class AmplitudeHelperFollowButtonImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeFollowButton {

    override fun followAction(
        fromId: Long,
        toId: Long,
        where: AmplitudeFollowButtonPropertyWhere,
        type: AmplitudePropertyType,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty
    ) {
        delegate.logEvent(
            eventName = FollowButtonConstants.FOLLOW_ACTION,
            properties = {
                it.apply {
                    addProperty(FOLLOW_BUTTON_FROM, fromId)
                    addProperty(FOLLOW_BUTTON_TO, toId)
                    addProperty(where)
                    addProperty(type)
                    addProperty(amplitudeInfluencerProperty)
                }
            }
        )
    }

    override fun logUnfollowAction(
        fromId: Long,
        toId: Long,
        where: AmplitudeFollowButtonPropertyWhere,
        type: AmplitudePropertyType,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty
    ) {
        delegate.logEvent(
            eventName = FollowButtonConstants.UNFOLLOW_ACTION,
            properties = {
                it.apply {
                    addProperty(FOLLOW_BUTTON_FROM, fromId)
                    addProperty(FOLLOW_BUTTON_TO, toId)
                    addProperty(where)
                    addProperty(type)
                    addProperty(amplitudeInfluencerProperty)
                }
            }
        )
    }
}
