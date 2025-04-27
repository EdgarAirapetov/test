package com.numplates.nomera3.modules.contentsharing.ui.rooms

import com.meera.core.base.viewmodel.State

data class SharingRoomsState(
    val isRedirecting: Boolean = false,
    val isLoading: Boolean = false,
    val query: String? = null
) : State
