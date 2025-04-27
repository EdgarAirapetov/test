package com.meera.media_controller_implementation.domain.analytic

internal interface MediaControllerAmplitude {
    fun logVideoAlertAction(
        actionType: MediaControllerAmplitudePropertyVideoAlertActionType,
        videoDurationSec: Int,
        maxVideoDurationSec: Int
    )
}
