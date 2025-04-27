package com.numplates.nomera3.modules.appDialogs.ui.show

import com.numplates.nomera3.modules.appDialogs.DialogType

sealed class DialogShowViewAction {
    data class DialogCompleted(val dialogType: DialogType): DialogShowViewAction()
    data class DialogNotCompleted(val dialogType: DialogType): DialogShowViewAction()
}