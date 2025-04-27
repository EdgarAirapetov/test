package com.numplates.nomera3.presentation.view.fragments.profilephoto

sealed class ProfilePhotoViewerEffect {
    data class OnSave(val position: Int) : ProfilePhotoViewerEffect()
    data class OnRemove(val position: Int) : ProfilePhotoViewerEffect()
    data class OnMakeAvatar(val position: Int) : ProfilePhotoViewerEffect()
}
