package com.meera.media_controller_api.model

sealed class MediaControllerCropBackendKey(val name: String) {
    data object Chat : MediaControllerCropBackendKey("chat")
    data object Gallery : MediaControllerCropBackendKey("gallery")
    data object Post : MediaControllerCropBackendKey("road")
    data object Profile : MediaControllerCropBackendKey("profile")
    data object PostPreview : MediaControllerCropBackendKey("preview")
    data object AnyAvatar : MediaControllerCropBackendKey("avatar")
    data object Complains : MediaControllerCropBackendKey("complains")
    data object EventPost : MediaControllerCropBackendKey("event_post")
    data object Moments : MediaControllerCropBackendKey("moments")
    data object Common : MediaControllerCropBackendKey("common")
}
