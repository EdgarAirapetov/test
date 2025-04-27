package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel

sealed class ProfilePhotoViewEvent {
    class OnPhotoUploadSuccess(var photoUrl: String?,var createAvatarPost: Int) : ProfilePhotoViewEvent()

    object OnPhotoUploadError : ProfilePhotoViewEvent()
    object AvatarRemovedSuccess : ProfilePhotoViewEvent()
    object AvatarRemovedError : ProfilePhotoViewEvent()
    class OnAnimatedAvatarSaved(val path: String) : ProfilePhotoViewEvent()

    class OnCreateAvatarPostSettings(
        val privacySettingModel: PrivacySettingModel?,
        val imagePath: String,
        val animation: String?,
        val isSendAvatarPost: Boolean = true
    ) : ProfilePhotoViewEvent()

}
