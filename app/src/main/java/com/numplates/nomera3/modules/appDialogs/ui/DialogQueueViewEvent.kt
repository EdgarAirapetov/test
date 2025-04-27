package com.numplates.nomera3.modules.appDialogs.ui

sealed class DialogQueueViewEvent {
    sealed class ShowDialog: DialogQueueViewEvent() {
        object Onboarding: ShowDialog()
        object EnableCalls: ShowDialog()
    }
}
