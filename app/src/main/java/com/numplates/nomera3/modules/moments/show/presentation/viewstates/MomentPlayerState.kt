package com.numplates.nomera3.modules.moments.show.presentation.viewstates

sealed class MomentPlayerState {
    object Start: MomentPlayerState()
    object Pause: MomentPlayerState()
    object Resume: MomentPlayerState()
    object Stop: MomentPlayerState()
    object Hide: MomentPlayerState()
}
