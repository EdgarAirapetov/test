package com.numplates.nomera3.modules.contentsharing.ui

import com.meera.core.base.viewmodel.Effect

sealed class ContentSharingEffect : Effect {

    data object SelectChatsToUpload : ContentSharingEffect()

    data object CloseSharingScreen : ContentSharingEffect()

    data object ShowWentWrongAlert : ContentSharingEffect()

    data object ShowNetworkAlert : ContentSharingEffect()

    data object ShowVideoDurationAlert : ContentSharingEffect()
}
