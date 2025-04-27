package com.numplates.nomera3.modules.contentsharing.ui.loader

import com.meera.core.base.viewmodel.Effect

sealed class SharingLoaderEffect : Effect {

    data object FinishLoading : SharingLoaderEffect()

    data object ShowWentWrongAlert : SharingLoaderEffect()
}
