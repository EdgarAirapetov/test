package com.numplates.nomera3.modules.appDialogs.ui.show

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.appDialogs.DialogType

class DialogShowViewModel : ViewModel() {

    val eventLiveData = MutableLiveData<DialogShowViewEvent>()

    fun doAction(action: DialogShowViewAction) {
        when (action) {
            is DialogShowViewAction.DialogCompleted -> dialogCompleted(action.dialogType)
            is DialogShowViewAction.DialogNotCompleted -> dialogNotCompleted(action.dialogType)
        }
    }

    private fun dialogCompleted(dialogType: DialogType) {
        event(DialogShowViewEvent.Completed(dialogType))
    }

    private fun dialogNotCompleted(dialogType: DialogType) {
        event(DialogShowViewEvent.NotCompleted(dialogType))
    }

    private fun event(event: DialogShowViewEvent) {
        eventLiveData.postValue(event)
    }
}