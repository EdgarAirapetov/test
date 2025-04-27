package com.numplates.nomera3.modules.reactionStatistics.ui.entity

import androidx.annotation.StringRes

sealed class ReactionsUiEvent {
    data class ShowErrorToast(@StringRes val message: Int) : ReactionsUiEvent()

    object ShowReactionsIsEmpty : ReactionsUiEvent()
}
