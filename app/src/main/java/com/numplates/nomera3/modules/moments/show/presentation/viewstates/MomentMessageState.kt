package com.numplates.nomera3.modules.moments.show.presentation.viewstates

import androidx.annotation.StringRes

sealed class MomentMessageState {
    data class ShowError(@StringRes val error: Int) : MomentMessageState()
    data class ShowSuccess(@StringRes val message: Int) : MomentMessageState()
}
