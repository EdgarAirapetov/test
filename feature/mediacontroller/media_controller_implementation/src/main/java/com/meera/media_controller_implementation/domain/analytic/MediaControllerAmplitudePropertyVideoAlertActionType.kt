package com.meera.media_controller_implementation.domain.analytic

import com.meera.application_api.analytic.model.AmplitudeProperty

internal enum class MediaControllerAmplitudePropertyVideoAlertActionType(val property: String) : AmplitudeProperty {
    EDITOR("editor"),
    CANCEL("cancel");

    override val _value: String
        get() = property

    override val _name: String
        get() = MediaControllerAmplitudePropertyNameConst.VIDEO_ALERT_ACTION_TYPE
}
