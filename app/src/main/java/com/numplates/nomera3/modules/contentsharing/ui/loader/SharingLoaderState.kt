package com.numplates.nomera3.modules.contentsharing.ui.loader

import com.meera.core.base.viewmodel.State

data class SharingLoaderState(
    val progress: Float = 0f,
    val isLoading: Boolean = false,
) : State
