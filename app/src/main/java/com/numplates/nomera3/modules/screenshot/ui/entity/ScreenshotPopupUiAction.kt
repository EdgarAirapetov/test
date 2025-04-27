package com.numplates.nomera3.modules.screenshot.ui.entity

import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotActionTypeProperty

sealed interface ScreenshotPopupUiAction {

    data class LogPopupOpen(val data: ScreenshotPopupData) : ScreenshotPopupUiAction

    data class LogShareAction(
        val actionType: AmplitudeScreenshotActionTypeProperty,
        val data: ScreenshotPopupData
    ) : ScreenshotPopupUiAction

}
