package com.numplates.nomera3.modules.contentsharing.ui.rooms

import com.meera.core.base.viewmodel.Effect

sealed class SharingRoomsEffect : Effect {

    data object SendNetworkError : SharingRoomsEffect()

    data object SendContentToChats : SharingRoomsEffect()

    data object SendVideoDurationError : SharingRoomsEffect()

    data object ShareContentToChats : SharingRoomsEffect()

    data object ScheduleScrollListToTop : SharingRoomsEffect()
}
