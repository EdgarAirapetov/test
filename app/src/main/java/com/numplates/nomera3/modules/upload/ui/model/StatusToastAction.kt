package com.numplates.nomera3.modules.upload.ui.model

sealed interface StatusToastAction {
    object RetryUpload : StatusToastAction
}
