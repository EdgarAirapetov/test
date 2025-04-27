package com.numplates.nomera3.modules.moments.show.presentation.viewstates

sealed class MomentComplainState {
    data class MomentComplainSuccess(val userId: Long) : MomentComplainState()
    object MomentComplainError : MomentComplainState()
}
