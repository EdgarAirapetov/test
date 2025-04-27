package com.numplates.nomera3.modules.baseCore.helper.amplitude.feed

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

const val HAVE_ACTION = "have action"
const val ANNOUNCE_NAME = "announce name"
const val ORIGINAL_VIDEO_DURATION = "original video duration"
const val VIDEO_DURATION_MAX = "video duration max"

enum class AmplitudeFeedEventName(
    private val event: String
) : AmplitudeName {
    ANNOUNCEMENT_BUTTON_PRESS("announcement button press"),
    VIDEO_ALERT_ACTION("video alert action");

    override val eventName: String
        get() = event
}

enum class AmplitudeAnnouncementButtonType(val property: String) : AmplitudeProperty {
    OPTIONAL_BUTTON("optional button"),
    CLEAR_BUTTON("clear button");

    override val _name: String
        get() = AmplitudePropertyNameConst.INPUT_TYPE

    override val _value: String
        get() = property
}

enum class AmplitudeVideoAlertActionType(val property: String) : AmplitudeProperty {
    EDITOR("editor"),
    CANCEL("cancel");

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE

    override val _value: String
        get() = property
}
