package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class UserRegisterViewEvent {

    class OnSuccessUpdateUserProfile(val isShouldGoBack: Boolean) : UserRegisterViewEvent()

    object OnFailureUpdateUserProfile : UserRegisterViewEvent()

    object OnFailureChangeAvatar : UserRegisterViewEvent()

    object OnGoneProgressUserAvatar : UserRegisterViewEvent()

}