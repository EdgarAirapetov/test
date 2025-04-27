package com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeMutualFriendsAnalytic {
    /**
     * Отмечаем как был открыт экран
     * [com.numplates.nomera3.presentation.view.fragments.UserSubscriptionsFriendsInfoFragment] или
     * [com.numplates.nomera3.presentation.view.fragments.UserMutualSubscriptionFragment]
     * @param friendTabSelected - Какой тип списка "Подписчики/Друзья/Подписки/Общие"
     * @param typeSelected - Передается тип как именно был открыт список:
     *        Через ViewPager/через профиль
     */
    fun logMutualFriendsTabSelected(
        friendTabSelected: AmplitudeSelectedMutualFriendsTabProperty,
        typeSelected: AmplitudeHowSelectedMutualFriendsProperty
    )
    fun logDisabledMutualFriendsClicked()
}

class AmplitudeMutualFriendsAnalyticImpl @Inject constructor(
    private val amplitudeDelegate: AmplitudeEventDelegate
) : AmplitudeMutualFriendsAnalytic {

    override fun logMutualFriendsTabSelected(
        friendTabSelected: AmplitudeSelectedMutualFriendsTabProperty,
        typeSelected: AmplitudeHowSelectedMutualFriendsProperty
    ) {
        amplitudeDelegate.logEvent(
            eventName = AmplitudeMutualFriendsTypeName.USER_AUDIENCE_TAP,
            properties = {
                it.apply {
                    addProperty(friendTabSelected)
                    addProperty(typeSelected)
                }
            }
        )
    }

    override fun logDisabledMutualFriendsClicked() {
        amplitudeDelegate.logEvent(
            eventName = AmplitudeMutualFriendsTypeName.UNCLICKABLE_USER_AUDIENCE_TAP
        )
    }
}
