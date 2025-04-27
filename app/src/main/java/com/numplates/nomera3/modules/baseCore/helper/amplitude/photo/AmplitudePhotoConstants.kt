package com.numplates.nomera3.modules.baseCore.helper.amplitude.photo

import com.meera.application_api.analytic.model.AmplitudeName

enum class AmplitudePhotoEventName(
    private val event: String
) : AmplitudeName {

    PHOTO_SCREEN_OPEN("photo screen open");

    override val eventName: String
        get() = event
}
