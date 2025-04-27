package com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySettingVisibility
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeSelectPrivacyShowFriends {
    fun onShowMutualFriendsPrivacySelected(actionType: AmplitudePropertySettingVisibility)
}

class AmplitudeSelectPrivacyShowFriendsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeSelectPrivacyShowFriends {

    override fun onShowMutualFriendsPrivacySelected(
        actionType: AmplitudePropertySettingVisibility
    ) {
        delegate.logEvent(
            eventName = AmplitudeMutualFriendsTypeName.PRIVACY_AUDIENCE_VISIBILITY_CHANGE,
            properties = { jsonObject ->
                jsonObject.apply {
                    addProperty(actionType)
                }
            }
        )
    }

}
