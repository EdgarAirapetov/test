package com.numplates.nomera3.modules.appDialogs.ui.show

import com.numplates.nomera3.modules.appDialogs.DialogType

sealed class DialogShowViewEvent {
    data class Completed(val dialogType: DialogType): DialogShowViewEvent()
    data class NotCompleted(val dialogType: DialogType): DialogShowViewEvent()
}
