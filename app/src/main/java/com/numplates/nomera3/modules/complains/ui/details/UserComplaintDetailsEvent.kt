package com.numplates.nomera3.modules.complains.ui.details

sealed class UserComplaintDetailsEvent {
    data class OnOpenImage(val path: String) : UserComplaintDetailsEvent()
    data class OnEditImage(val path: String) : UserComplaintDetailsEvent()
    data class OnVideoPlay(val path: String) : UserComplaintDetailsEvent()
    data class OnEditVideo(val path: String) : UserComplaintDetailsEvent()
    data class FinishComplaintFlow(val isSuccess: Boolean) : UserComplaintDetailsEvent()
}
