package com.numplates.nomera3.modules.contentsharing.ui.loader

import com.meera.core.base.viewmodel.Action

sealed class SharingLoaderAction : Action {

    data object SendSharingData : SharingLoaderAction()

    data object CancelDataUploading : SharingLoaderAction()
}
