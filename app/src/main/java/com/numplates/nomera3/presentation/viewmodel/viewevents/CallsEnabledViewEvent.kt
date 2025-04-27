package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.presentation.view.widgets.CustomRowSelector

sealed class CallsEnabledViewEvent {
    class SettingSaved(
            var model: CustomRowSelector.CustomRowSelectorModel
    ): CallsEnabledViewEvent()
    object SettingSavedError: CallsEnabledViewEvent()
}