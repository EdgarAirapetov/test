package com.numplates.nomera3.presentation.view.fragments.profilephoto

sealed class ProfilePhotoViewerAction {

    data class Save(val position: Int) : ProfilePhotoViewerAction()
    data class Remove(val position: Int) : ProfilePhotoViewerAction()
    data class MakeAvatar(val position: Int) : ProfilePhotoViewerAction()
}
