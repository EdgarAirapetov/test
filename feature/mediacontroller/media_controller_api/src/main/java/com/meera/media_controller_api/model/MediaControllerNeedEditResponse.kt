package com.meera.media_controller_api.model

sealed class MediaControllerNeedEditResponse {
    object NeedToCrop : MediaControllerNeedEditResponse()
    data class VideoTooLong(val currentDurationSec: Int, val maxDurationSec: Int) : MediaControllerNeedEditResponse()
    object NoNeedToEdit : MediaControllerNeedEditResponse()
}
