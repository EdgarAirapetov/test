package com.numplates.nomera3.modules.screenshot.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotActionTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotWhereProperty
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupUiAction
import javax.inject.Inject

class ScreenshotPopupViewModel @Inject constructor(
    private val screenshotAnalytics: AmplitudeScreenshotAnalytics,
    private val getUserUidUseCase: GetUserUidUseCase
) : ViewModel() {

    fun handleUiAction(action: ScreenshotPopupUiAction) {
        when (action) {
            is ScreenshotPopupUiAction.LogPopupOpen -> logPopupOpen(action.data)
            is ScreenshotPopupUiAction.LogShareAction -> logShareAction(action.actionType, action.data)
        }
    }

    private fun logPopupOpen(data: ScreenshotPopupData) {
        val where = getWherePropertyFromScreenshotPlace(data.screenshotPlace)
        screenshotAnalytics.logScreenshotShareOpen(
            whereProperty = where,
            fromId = getUserUidUseCase.invoke(),
            profileId = data.profileId,
            eventId = data.eventId,
            momentId = data.momentId,
            postId = data.postId,
            communityId = data.communityId
        )
    }

    private fun logShareAction(actionType: AmplitudeScreenshotActionTypeProperty, data: ScreenshotPopupData) {
        val where = getWherePropertyFromScreenshotPlace(data.screenshotPlace)
        screenshotAnalytics.logScreenshotShareAction(
            actionTypeProperty = actionType,
            whereProperty = where,
            fromId = getUserUidUseCase.invoke(),
            profileId = data.profileId,
            eventId = data.eventId,
            momentId = data.momentId,
            postId = data.postId,
            communityId = data.communityId
        )
    }

    private fun getWherePropertyFromScreenshotPlace(screenshotPlace: ScreenshotPlace): AmplitudeScreenshotWhereProperty {
        return when (screenshotPlace) {
            ScreenshotPlace.OWN_PROFILE -> AmplitudeScreenshotWhereProperty.OWN_PROFILE
            ScreenshotPlace.USER_PROFILE -> AmplitudeScreenshotWhereProperty.USER_PROFILE
            ScreenshotPlace.MAP_EVENT -> AmplitudeScreenshotWhereProperty.MAP_EVENT
            ScreenshotPlace.POST_EVENT -> AmplitudeScreenshotWhereProperty.POST_EVENT
            ScreenshotPlace.MOMENT -> AmplitudeScreenshotWhereProperty.MOMENT
            ScreenshotPlace.FEED_POST -> AmplitudeScreenshotWhereProperty.FEED_POST
            ScreenshotPlace.COMMUNITY -> AmplitudeScreenshotWhereProperty.COMMUNITY
            ScreenshotPlace.COMMUNITY_POST -> AmplitudeScreenshotWhereProperty.COMMUNITY_POST
            ScreenshotPlace.OTHER -> AmplitudeScreenshotWhereProperty.OTHER
        }
    }

}
