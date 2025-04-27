package com.numplates.nomera3.modules.feedviewcontent.presentation.viewstates

import androidx.annotation.StringRes

sealed class ViewContentMessageState {
    data class ShowError(@StringRes val error: Int) : ViewContentMessageState()
    data class ShowSuccess(@StringRes val message: Int) : ViewContentMessageState()
}

