package com.numplates.nomera3.modules.upload.ui.model

sealed interface StatusToastState {
    data class Progress(
        val message: String?
        ) : StatusToastState
    data class Success(
        val message: String?
    ) : StatusToastState
    object Error : StatusToastState
    data class Info(
        val message: String
    ) : StatusToastState
}
